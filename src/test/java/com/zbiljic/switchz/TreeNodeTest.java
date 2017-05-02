package com.zbiljic.switchz;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.testng.Assert.fail;

public class TreeNodeTest {

  private Function<String, String> fakeHandler(final String value) {
    return new Function<String, String>() {
      @Override
      public String apply(String s) {
        return value;
      }
    };
  }

  private static class TestRequest {

    String path;
    boolean nullHandler;
    String route;
    Map<String, String> ps;

    TestRequest(String path, boolean nullHandler, String route, Map<String, String> ps) {
      this.path = path;
      this.nullHandler = nullHandler;
      this.route = route;
      this.ps = ps;
    }
  }

  void checkRequests(TreeNode<Function<String, String>> tree, TestRequest[] testRequests) {
    for (TestRequest requests : testRequests) {
      NodeMatch<Function<String, String>> match = tree.get(requests.path);

      Function<String, String> handler = match.getValue();
      Map<String, String> ps = match.getParameters();

      if (handler == null) {
        if (!requests.nullHandler) {
          fail(String.format("handler mismatch for route '%s': Expected non-null handler", requests.path));
        }
      } else if (requests.nullHandler) {
        fail(String.format("handler mismatch for route '%s': Expected null handler", requests.path));
      } else {
        String handlerResult = handler.apply(null);
        if (!Objects.equals(handlerResult, requests.route)) {
          fail(String.format("handler mismatch for route '%s': Wrong handler (%s != %s)",
            requests.path, handlerResult, requests.route));
        }
      }

      if (requests.ps != null && !match.isTrailingSlashRedirect()) {
        if (!equalMaps(requests.ps, ps)) {
          fail(String.format("Params mismatch for route '%s'", requests.path));
        }
      }
    }
  }

  <K, V> boolean equalMaps(Map<K, V> m1, Map<K, V> m2) {
    if (m1 == null || m2 == null) {
      return m2 == null;
    }
    return m1.equals(m2);
  }

  int checkPriorities(TreeNode<?> n) {
    int priority = 0;

    for (int i = 0; i < n.children.length; i++) {
      priority += checkPriorities(n.children[i]);
    }

    if (n.value != null) {
      priority++;
    }

    if (n.priority != priority) {
      fail(String.format("priority mismatch for node '%s': is %d, should be %d",
        n.path, n.priority, priority));
    }

    return priority;
  }

  short checkMaxParams(TreeNode<?> n) {
    short maxParams = 0;

    for (int i = 0; i < n.children.length; i++) {
      short params = checkMaxParams(n.children[i]);
      if (params > maxParams) {
        maxParams = params;
      }
    }

    if (n.nodeType.ordinal() > NodeType.ROOT.ordinal() && !n.wildChild) {
      maxParams++;
    }

    if (n.maxParams != maxParams) {
      fail(String.format("maxParams mismatch for node '%s': is %d, should be %d",
        n.path, n.maxParams, maxParams));
    }

    return maxParams;
  }

  @Test
  public void testTreeAddAndGet() throws Exception {

    final TreeNode<Function<String, String>> tree = new TreeNode<>();

    String[] routes = new String[]{
      "/hi",
      "/contact",
      "/co",
      "/c",
      "/a",
      "/ab",
      "/doc/",
      "/doc/go_faq.html",
      "/doc/go1.html",
      "/α",
      "/β",
    };

    for (String route : routes) {
      tree.add(route, fakeHandler(route));
    }

    TreeNodeUtil.dumpTree(tree);

    checkRequests(tree, new TestRequest[]{
      new TestRequest("/a", false, "/a", null),
      new TestRequest("/", true, "", null),
      new TestRequest("/hi", false, "/hi", null),
      new TestRequest("/contact", false, "/contact", null),
      new TestRequest("/co", false, "/co", null),
      new TestRequest("/con", true, "", null),  // key mismatch
      new TestRequest("/cona", true, "", null), // key mismatch
      new TestRequest("/no", true, "", null),   // no matching child
      new TestRequest("/ab", false, "/ab", null),
      new TestRequest("/α", false, "/α", null),
      new TestRequest("/β", false, "/β", null),
    });

    checkPriorities(tree);
    checkMaxParams(tree);
  }

  @Test
  public void testTreeWildcard() throws Exception {

    final TreeNode<Function<String, String>> tree = new TreeNode<>();

    String[] routes = new String[]{
      "/",
      "/cmd/:tool/:sub",
      "/cmd/:tool/",
      "/src/*filepath",
      "/search/",
      "/search/:query",
      "/user_:name",
      "/user_:name/about",
      "/files/:dir/*filepath",
      "/doc/",
      "/doc/go_faq.html",
      "/doc/go1.html",
      "/info/:user/public",
      "/info/:user/project/:project",
    };

    for (String route : routes) {
      tree.add(route, fakeHandler(route));
    }

    TreeNodeUtil.dumpTree(tree);

    checkRequests(tree, new TestRequest[]{
      new TestRequest("/", false, "/", null),
      new TestRequest("/cmd/test/", false, "/cmd/:tool/", new HashMap<String, String>() {{
        put("tool", "test");
      }}),
      new TestRequest("/cmd/test", true, "", new HashMap<String, String>() {{
        put("tool", "test");
      }}),
      new TestRequest("/cmd/test/3", false, "/cmd/:tool/:sub", new HashMap<String, String>() {{
        put("tool", "test");
        put("sub", "3");
      }}),
      new TestRequest("/src/", false, "/src/*filepath", new HashMap<String, String>() {{
        put("filepath", "/");
      }}),
      new TestRequest("/src/some/file.png", false, "/src/*filepath", new HashMap<String, String>() {{
        put("filepath", "/some/file.png");
      }}),
      new TestRequest("/search/", false, "/search/", null),
      new TestRequest("/search/someth!ng+in+ünìcodé", false, "/search/:query", new HashMap<String, String>() {{
        put("query", "someth!ng+in+ünìcodé");
      }}),
      new TestRequest("/search/someth!ng+in+ünìcodé/", true, "", new HashMap<String, String>() {{
        put("query", "someth!ng+in+ünìcodé");
      }}),
      new TestRequest("/user_go", false, "/user_:name", new HashMap<String, String>() {{
        put("name", "go");
      }}),
      new TestRequest("/user_go/about", false, "/user_:name/about", new HashMap<String, String>() {{
        put("name", "go");
      }}),
      new TestRequest("/files/js/inc/framework.js", false, "/files/:dir/*filepath", new HashMap<String, String>() {{
        put("dir", "js");
        put("filepath", "/inc/framework.js");
      }}),
      new TestRequest("/info/gordon/public", false, "/info/:user/public", new HashMap<String, String>() {{
        put("user", "gordon");
      }}),
      new TestRequest("/info/gordon/project/go", false, "/info/:user/project/:project", new HashMap<String, String>() {{
        put("user", "gordon");
        put("project", "go");
      }}),
    });

    checkPriorities(tree);
    checkMaxParams(tree);
  }
}
