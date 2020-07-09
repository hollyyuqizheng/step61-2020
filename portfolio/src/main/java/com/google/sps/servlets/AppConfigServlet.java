package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/appConfigServlet")
public class AppConfigServlet extends HttpServlet {

  private final String PARAM_API_KEY = "API_KEY";

  /** Puts a JSON object with the API key into the Get request response. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map<String, String> env = System.getenv();
    String apiKey = env.get(PARAM_API_KEY);
    Map<String, String> responseMap = new HashMap<String, String>();
    responseMap.put(PARAM_API_KEY, apiKey);

    Gson gson = new Gson();
    String keyJson = gson.toJson(responseMap);
    response.setContentType("json");
    response.getWriter().println(keyJson);
  }
}
