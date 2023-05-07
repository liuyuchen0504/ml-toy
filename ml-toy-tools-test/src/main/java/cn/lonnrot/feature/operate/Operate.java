package cn.lonnrot.feature.operate;

interface Operate <U, R> {
  R call(U a, U b);
}
