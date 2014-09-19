/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime;

import com.diosay.otpl.runtime.opcodes.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import mano.util.Utility;

/**
 * 操作码载入器
 *
 * @author jun <jun@diosay.com>
 */
public class CodeLoader {

    private InputStream input;
    private String version;
    private Charset encoding;// = Charset.forName("utf-8");
    byte[] buf = new byte[8];
    private HashMap<Integer, OpCode> codes = new HashMap<>();
    private File source;
    public CodeLoader parent;
    public CodeLoader child;
    public int pageAddr;

    void close() {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ex) {
            }
        }
    }

    public File getSource() {
        return source;
    }

    public boolean load(ExecutionContext context, InputStream in, boolean b, File source) throws IOException {
        encoding = context.inputEncoding();
        this.source = source;
        input = in;

        if (input.read(buf, 0, 8) != 8) { //head line
            close();
            throw new mano.InvalidOperationException("无效的OTPL文件。");
        }
        version = new String(buf, 0, 7, encoding);//OTPL-01

        byte mode = buf[7];
        //create time //8
        if (input.read(buf, 0, 8) != 8) { //创建时间
            close();
            throw new mano.InvalidOperationException("无效的OTPL文件。");
        }
        if (mode == 1) {//检查文件 改变
            if (input.read(buf, 0, 8) != 8) { //修改时间
                close();
                throw new mano.InvalidOperationException("无效的OTPL文件。");
            }
            long modified = Utility.toLong(buf, 0);//8
            File file = new File(new String(loadString(), encoding));
            if (!file.exists() || !file.isFile() || modified != file.lastModified()) {
                close();
                return false;
            }
        }
        readToEhead();

        return startAddr != 0;
    }

    int startAddr = 0;
    int endAddr = -1;

    private void readToEhead() throws IOException {
        while (true) {

            OpCode code = loadCode();

            if (code == null) {

            } else if (code.getType() == OpcodeType.stblk) {
                codes.put(code.getAddress(), code);
                Block blk = (Block) code;
                this.blocks.put(blk.getName(), blk);
            } else if (code.getType() == OpcodeType.ehead) {
                codes.put(code.getAddress(), code);
                startAddr = code.getAddress();
                return;
            } else {
                codes.put(code.getAddress(), code);
            }
        }
    }

    private OpCode loadCode() throws IOException {

        int read = input.read(buf, 0, 5);
        if (read != 5) { //head line
            close();
            throw new mano.InvalidOperationException("无效的OTPL文件：读取行头失败。");
        }
        int addr = Utility.toInt(buf, 0);
        OpcodeType type = OpcodeType.parse(buf, 4);

        if (type.equals(OpcodeType.abort)) {
            EndOfFile code = new EndOfFile();
            code.setAddress(addr);
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.call)) {
            Call code = new Call();
            code.setAddress(addr);
            code.setArgLength(loadInt());

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.callvri)) {
            Callvri code = new Callvri();
            code.setAddress(addr);
            code.setArgLength(loadInt());
            code.setName(new String(loadString(), this.encoding));

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.doc)) {
            Layout code = new Layout();
            code.setAddress(addr);
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.body)) {
            Body code = new Body();
            code.setAddress(addr);
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ehead)) {
            EndHeader code = new EndHeader();
            code.setAddress(addr);
            code.setLoader(this);
            return code;
        }  else if (type.equals(OpcodeType.br)) {
            Break code = new Break();
            code.setAddress(addr);
            code.setBehavior(loadBytes(1)[0]);
            if (code.getBehavior() != Break.BREAK_EXIT) {
                code.setTarget(loadInt());
            }
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ldu)) {
            LoadLong code = new LoadLong();
            code.setAddress(addr);
            code.setValue(loadLong());
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ldm)) {
            LoadMember code = new LoadMember();
            code.setAddress(addr);
            code.setArgLength(loadInt());
            code.setName(new String(loadString(), this.encoding));

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ldr)) {
            LoadReal code = new LoadReal();
            code.setAddress(addr);
            code.setValue(Double.parseDouble(new String(this.loadString(), this.encoding)));
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ldstr)) {
            LoadString code = new LoadString();
            code.setAddress(addr);
            code.setContent(new String(this.loadString(), this.encoding));
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ldv)) {
            LoadVariable code = new LoadVariable();
            code.setAddress(addr);
            code.setName(new String(this.loadString(), this.encoding));
            code.setLoader(this);
            return code;
        } 
        else if (type.equals(OpcodeType.nop)) {
            Nop code = new Nop();
            code.setAddress(addr);
            code.setLoader(this);
            return code;
        }  else if (type.equals(OpcodeType.pop)) {
            Pop code = new Pop();
            code.setAddress(addr);

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.prt)) {
            Print code = new Print();
            code.setAddress(addr);

            if (input.read(buf, 0, 1) != 1) {
                close();
                throw new mano.InvalidOperationException("无效的OTPL文件，读取参数失败。");
            }
            code.setFiltrable(buf[0] == 1);
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.stblk)) {
            Block code = new Block();
            code.setAddress(addr);
            code.setBeginAddress(this.loadInt());
            code.setName(new String(loadString(), this.encoding));

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.call_blk)) {
            CallBlock code = new CallBlock();
            code.setAddress(addr);
            code.setName(new String(loadString(), this.encoding));
            if (input.read(buf, 0, 1) != 1) { //长度
                close();
                throw new mano.InvalidOperationException("获取布尔值失败。");
            }
            code.required(buf[0] == 1);

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.stv)) {
            SetVariable code = new SetVariable();
            code.setAddress(addr);

            code.setName(new String(loadString(), this.encoding));
            code.setLoader(this);
            return code;
        }  else if (type.equals(OpcodeType.ptstr)) {
            PrintString code = new PrintString();
            code.setAddress(addr);

            byte[] bytes = loadString();
            code.setBytes(bytes, 0, bytes.length);
            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.op)) {
            Operator code = new Operator();
            code.setAddress(addr);
            code.setOperator(loadBytes(1)[0]);

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.peek)) {
            Peek code = new Peek();
            code.setAddress(addr);

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.ldc)) {
            LoadConst code = new LoadConst();
            code.setAddress(addr);
            code.setValue(loadBytes(1)[0]);

            code.setLoader(this);
            return code;
        } else if (type.equals(OpcodeType.sl)) {
            SourceLineNumber code = new SourceLineNumber();
            code.setAddress(addr);
            code.setValue(loadInt());
            code.setLoader(this);
            return code;
        } else {
            throw new java.lang.RuntimeException("OTPL操作码类型未定义:" + type);
        }
    }

    private byte[] loadString() throws IOException {
        if (input.read(buf, 0, 4) != 4) { //长度
            close();
            throw new mano.InvalidOperationException("获取字符串长度。");
        }
        int len = Utility.toInt(buf, 0);
        byte[] bytes = new byte[len];
        if (input.read(bytes) != len) {
            close();
            throw new mano.InvalidOperationException("无效的OTPL文件，读取字符串失败。");
        }
        return bytes;
    }

    private int loadInt() throws IOException {
        if (input.read(buf, 0, 4) != 4) { //长度
            close();
            throw new mano.InvalidOperationException("获取int失败。");
        }
        return Utility.toInt(buf, 0);
    }

    private byte[] loadBytes(int len) throws IOException {
        if (input.read(buf, 0, len) != len) { //长度
            close();
            throw new mano.InvalidOperationException("获取字节失败。");
        }
        return buf;
    }

    private long loadLong() throws IOException {
        if (input.read(buf, 0, 8) != 8) { //长度
            close();
            throw new mano.InvalidOperationException("获取int失败。");
        }
        return Utility.toLong(buf, 0);
    }

    /**
     * 读取一个操作码
     *
     * @param addr
     * @return
     */
    public OpCode loadCode(int addr) throws IOException {
        if (this.endAddr != -1 && addr > this.endAddr) {
            return null;
        }
        if (this.codes.containsKey(addr)) {
            return this.codes.get(addr);
        }
        while (true) {
            OpCode code = loadCode();
            if (code == null) {
                return null;
            }
            this.codes.put(code.getAddress(), code);
            if (code.getType().equals(OpcodeType.abort)) {
                this.endAddr = code.getAddress();
                this.close();
            }
            if (code.getAddress() == addr) {
                return code;
            }
        }
    }
    private HashMap<String, Block> blocks = new HashMap<>();

    /**
     * 获取一个块
     *
     * @param name
     * @return
     */
    public Block getBlock(String name) {

        Block result = null;
        if (this.child != null && this.child.blocks.containsKey(name)) {
            result = this.child.blocks.get(name);
        }

        if (result == null && this.blocks.containsKey(name)) {
            result = this.blocks.get(name);
        }

        return result != null ? result : (this.child != null ? this.child.getBlock(name) : null);
    }

}
