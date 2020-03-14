/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.model.*;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.validation.FormUrlEncodedMessageValidator;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private HttpServer todoListServer;

    @Test
    @CitrusTest
    public void testPlainFormData() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todo")
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        http(httpActionBuilder -> httpActionBuilder
            .server(todoListServer)
            .receive()
            .post("/api/todo")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .messageType(MessageType.PLAINTEXT)
            .payload("{title=[${todoName}], description=[${todoDescription}]}"));

        http(httpActionBuilder -> httpActionBuilder
            .server(todoListServer)
            .respond(HttpStatus.OK));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testFormData() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todo")
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        http(httpActionBuilder -> httpActionBuilder
            .server(todoListServer)
            .receive()
            .post("/api/todo")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .messageType(FormUrlEncodedMessageValidator.MESSAGE_TYPE)
            .payload(getFormData(), new FormMarshaller()));

        http(httpActionBuilder -> httpActionBuilder
            .server(todoListServer)
            .respond(HttpStatus.OK));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

    private FormData getFormData() {
        FormData formData = new FormData();

        formData.setAction("/api/todo");
        formData.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        Control title = new Control();
        title.setName("title");
        title.setValue("${todoName}");
        formData.addControl(title);

        Control description = new Control();
        description.setName("description");
        description.setValue("@ignore@");
        formData.addControl(description);

        return formData;
    }

    @Test
    @CitrusTest
    public void testFormDataXml() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .send()
            .post("/api/todo")
            .fork(true)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .payload("title=${todoName}&description=${todoDescription}"));

        http(httpActionBuilder -> httpActionBuilder
            .server(todoListServer)
            .receive()
            .post("/api/todo")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .messageType(FormUrlEncodedMessageValidator.MESSAGE_TYPE)
            .payload("<form-data xmlns=\"http://www.citrusframework.org/schema/http/message\">" +
                        "<content-type>application/x-www-form-urlencoded</content-type>" +
                        "<action>/api/todo</action>" +
                        "<controls>" +
                            "<control name=\"title\">" +
                                "<value>${todoName}</value>" +
                            "</control>" +
                            "<control name=\"description\">" +
                                "<value>${todoDescription}</value>" +
                            "</control>" +
                        "</controls>" +
                    "</form-data>"));

        http(httpActionBuilder -> httpActionBuilder
            .server(todoListServer)
            .respond(HttpStatus.OK));

        http(httpActionBuilder -> httpActionBuilder
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK));
    }

}
