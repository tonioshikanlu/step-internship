// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/** Servlet that returns user data content.*/
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/html");
    /** Stores logged in user data or anonymus user data*/
    List<String> login_array = new ArrayList<>();
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      login_array.add("Hello " + userService.getCurrentUser().getEmail() + "!");
      login_array.add("<p> <a href=\"" + userService.createLogoutURL("/index.html") + "\">Logout</a></p>");
      login_array.add(Boolean.toString(userService.isUserLoggedIn()));
      Gson gson = new Gson();
      String json = gson.toJson(login_array);
      response.getWriter().println(json);
    } else {
      login_array.add("<p>Hello stranger.</p>");
      login_array.add("<p><a href=\"" + userService.createLoginURL("/index.html") + "\">Login</a></p>");
      login_array.add(Boolean.toString(userService.isUserLoggedIn()));
      Gson gson = new Gson();
      String json = gson.toJson(login_array);
      response.getWriter().println(json);
    }
  }
}

