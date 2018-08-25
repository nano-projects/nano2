/*
 * Copyright 2015-2018 the original author or authors.
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
package org.nanoframework.plugins.graphql;

import graphql.GraphQL;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;

/**
 * @author yanghe
 * @since 2.0.0
 */
public class HelloWorldDemo {

    public static void main(String[] args) {
        var schema = "type Query{hello: String} schema{query: Query}";

        var schemaParser = new SchemaParser();
        var typeDefinitionRegistry = schemaParser.parse(schema);

        var runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("world"))).build();

        var schemaGenerator = new SchemaGenerator();
        var graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        var build = GraphQL.newGraphQL(graphQLSchema).build();
        var executionResult = build.execute("{hello}");

        System.out.println(executionResult.getData().toString());
        // Prints: {hello=world}
    }
}
