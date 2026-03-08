package io;

import org.junit.jupiter.api.Test;

public class TestMain {

    @Test
    public void test1() {
//        int i = 255;
        for (int i = 0; i <= 500; i++) {
            System.out.println("i=:[" + i + "]");
            System.out.println("二进制" + Integer.toBinaryString(i));
            System.out.println("负数二进制" + Integer.toBinaryString(-i));
            System.out.println("与运算结果：" + (i & (-i)));
            System.out.println("=======");
        }
    }
}
