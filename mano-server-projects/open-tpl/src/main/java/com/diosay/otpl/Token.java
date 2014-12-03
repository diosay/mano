/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl;

import java.util.ArrayList;

/**
 * 表示一个词法元素。 
 * @author jun <jun@diosay.com>
 */
public class Token extends ArrayList<Token>{

        public static final int ID = 1;
        public static final int REAL = 2;
        public static final int LONG = 3;
        public static final int STR = 4;
        public static final int BLK = 5;
        
        public static final int MUL = 6; // *
        public static final int DIV = 7; // /
        public static final int MOD = 8; // %
        
        public static final int ADD = 9; // +
        public static final int SUB = 10; // -
        
        public static final int ASSIGN = 11; // =
        
        public static final int OR = 12; // ||
        public static final int AND = 13; // &&
        
        public static final int GT = 14; // >
        public static final int GTE = 15; // >=
        public static final int LT = 16; // <
        public static final int LTE = 17; // <=
        
        public static final int EQ = 18; // ==
        public static final int NEQ = 19; // !=
        
        public static final int QM = 20; // ?
        public static final int DOT = 21; // .
        public static final int COMMA = 22; // ,
        public static final int COLON = 23; // :
        public static final int EM = 24; // !
        public static final int NC = 25; // ??
        
        
        public static final int OP = 26; // (
        public static final int CP = 27; // )
        public static final int OB = 28; // [
        public static final int CB = 29; // ]
        
        public static final int VL = 31; // |
        
        
        public int type;
        public String code;
        public int line;
        //public ArrayList<Token> children = new ArrayList<>();
        
        @Override
        public String toString(){
            return super.toString()+" type="+type+" code="+code;
        }
}
