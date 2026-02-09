package com.mk;

import com.mk.assistant.JavaAgent;
import com.mk.tools.InterviewAppointmentTool;
import com.mk.util.ToolsExcutorUtils;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.InterruptableAction;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


/**
 * Created with IntelliJ IDEA.
 *
 * @Author: milk
 * @Date: 2025/12/15/19:07
 * @Description:
 */
@SpringBootTest
public class MyTest {
    @Autowired
    private JavaAgent javaAgent;
    @Autowired
    private ToolsExcutorUtils toolsExcutorUtils;
    @Autowired
    private InterviewAppointmentTool interviewAppointmentTool;

    @Test
    public void test2() {

        //ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 1000, ThreadPoolExecutor.DiscardPolicy(), new ReentrantLock());

//        if(interviewAppointmentTool == null) System.out.println("interviewAppointmentTool is null");
//        List<Object> list = List.of(interviewAppointmentTool);
//        Map<String, ToolExecutor> toolExecutors = toolsExcutorUtils.tools(list);
//        System.out.println("Available tools:" + toolExecutors);
//        toolExecutors.forEach((name, executor) -> {
//            System.out.println("Tool name: " + name);
//        });
    }
    @Test
    public void test() throws GraphStateException {
        RetrievalAugmentor

        node1 node1 = new node1();
        node2 node2 = new node2();
        node1 node3 = new node1();
        node2 node4 = new node2();


        var stateGraph = new StateGraph<>(State.SCHEMA, initData -> new State(initData))
                .addNode("greeter", node_async(node1))
                .addNode("responder", node_async(node2))
                .addNode("greeter2", node_async(node3))
                .addNode("responder2", node_async(node4))
                // Define edges
                .addEdge(START, "greeter") // Start with the greeter node
                .addEdge("responder", END)
                .addEdge("greeter2", "responder")
                .addEdge("responder2", "responder")
                .addConditionalEdges("greeter", state -> {
                            boolean hasGreeting = state.getMessages().contains("Hello from GreeterNode!");
                            return CompletableFuture.completedFuture(String.valueOf(hasGreeting));
                        },
                        Map.of("true", "greeter2", "false", "responder2"));

        // Compile the graph

        var compiledGraph = stateGraph.compile();


        GraphRepresentation demo = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "demo");
        System.out.println(demo.toString());
        for (var item : compiledGraph.stream( Map.of( State.MESSAGES_KEY, "Let's, begin!" ) ) ) {
            System.out.println( item );
        }
    }


    public class State extends AgentState{
        public static final String MESSAGES_KEY = "question";

        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                MESSAGES_KEY, Channels.appender(ArrayList::new)
        );
        public State(Map<String, Object> initData) {
            super(initData);
        }
        public ArrayList<String> getMessages() {
            return value(MESSAGES_KEY, ArrayList::new);
        }
    }

    public class node1 implements NodeAction<State>{

        @Override
        public Map<String, Object> apply(State state) throws Exception {
            System.out.println("Node1 executing. Current messages: " + state.getMessages());
            return Map.of(State.MESSAGES_KEY, "Hello from GreeterNode!");
        }
    }

    public class node2 implements NodeAction<State>{

        @Override
        public Map<String, Object> apply(State state) throws Exception {
            System.out.println("Node2 executing. Current messages: " + state.getMessages());
            List<String> currentMessages = state.getMessages();
            if (currentMessages.contains("Hello from GreeterNode!")) {
                return Map.of(State.MESSAGES_KEY, "Acknowledged greeting!");
            }
            return Map.of(State.MESSAGES_KEY, "No greeting found.");

        }
    }
}
