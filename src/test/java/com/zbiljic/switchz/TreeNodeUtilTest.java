package com.zbiljic.switchz;

import org.testng.annotations.Test;

import static org.testng.Assert.fail;

public class TreeNodeUtilTest {

  @Test
  public void testCountParams() throws Exception {
    if (TreeNodeUtil.countParams("/path/:param1/static/*catch-all") != 2) {
      fail();
    }
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < 256; i++) {
      path.append("/:param");
    }
    if (TreeNodeUtil.countParams(path.toString()) != 256) {
      fail();
    }
  }

}
