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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;


/** Servlet that returns some example content.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {
	/** Store different messages as elements in an array to be displayed*/
	private ArrayList<String> list;
	@Override
  	public void init() {
 		list = new ArrayList<String>();
	}

 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 	  /** Initialize query to retrieve comments from datastore*/
    Query query = new Query("Task");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    /** Iterate over entities in datastore and retrieve each comment*/
    Integer counter = 0;
    List<String> tasks = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      /** Read in comment and maximum comment number*/
      String comment = (String) entity.getProperty("comment");
      String max_str = request.getParameter("max");
      /** Error checking to ensure null values aren't passed*/
      if (max_str == null){max_str = "5";};
      Integer max_num = Integer.parseInt(max_str);
      
      if (max_num > counter){
        tasks.add(comment);
      };
      counter ++;
    }
  /** Convert to json string using gson*/
    Gson gson = new Gson();
  	String json = gson.toJson(tasks);
  	response.setContentType("application/json;");
    response.getWriter().println(json);

  }
 @Override
 public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Accepts comment and updates list to show messages.
    String commentString = request.getParameter("comment");
    list.add(commentString);

    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("comment", commentString);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    // // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }


}
