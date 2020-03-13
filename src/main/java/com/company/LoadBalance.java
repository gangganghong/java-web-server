package com.company;

public class LoadBalance {

//    轮询调度算法
    public static int roundRobin(int total, int currentIndex) {

        int res = Math.floorMod(currentIndex + 1, total);

        return res;
    }
}
