package io.neoterm.utils;

/**
 * @author kiva
 */

public class StringDistance {
  public static int distance(String source, String target) {
    char[] sources = source.toCharArray();
    char[] targets = target.toCharArray();
    int sourceLen = sources.length;
    int targetLen = targets.length;

    int[][] d = new int[sourceLen + 1][targetLen + 1];
    for (int i = 0; i <= sourceLen; i++) {
      d[i][0] = i;
    }
    for (int i = 0; i <= targetLen; i++) {
      d[0][i] = i;
    }

    for (int i = 1; i <= sourceLen; i++) {
      for (int j = 1; j <= targetLen; j++) {
        if (sources[i - 1] == targets[j - 1]) {
          d[i][j] = d[i - 1][j - 1];
        } else {
          int insert = d[i][j - 1] + 1;
          int delete = d[i - 1][j] + 1;
          int replace = d[i - 1][j - 1] + 1;
          d[i][j] = Math.min(Math.min(insert, delete), Math.min(delete, replace));
        }
      }
    }
    return d[sourceLen][targetLen];
  }
}
