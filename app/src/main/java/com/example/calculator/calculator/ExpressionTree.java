package com.example.calculator.calculator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class ExpressionTree {
    private Node root;

    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();

    static {
        PRECEDENCE.put("+", 1);
        PRECEDENCE.put("-", 1);
        PRECEDENCE.put("*", 2);
        PRECEDENCE.put("/", 2);
    }

    private boolean isOperator(String s) {
        return PRECEDENCE.containsKey(s);
    }

    private int getPrecedence(String op) {
        return PRECEDENCE.getOrDefault(op, 0);
    }

    private Queue<String> infixToPostfix(String infixExpression) {
        Queue<String> outputQueue = new LinkedList<>();
        Stack<String> operatorStack = new Stack<>();

        String[] tokens = infixExpression.replaceAll("\\s+", "")
                .split("(?<=[-+*/()])|(?=[-+*/()])");

        String previousToken = "";

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.isEmpty()) {
                continue;
            }

            if (token.equals("-") &&
                    (i == 0 || previousToken.equals("(") || isOperator(previousToken))) {
                outputQueue.add("0");
                operatorStack.push("-");
                previousToken = token;
                continue;
            }

            if (Character.isDigit(token.charAt(0)) || (token.length() > 1 && token.charAt(0) == '-' && Character.isDigit(token.charAt(1)))) {
                outputQueue.add(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty() && isOperator(operatorStack.peek()) &&
                        getPrecedence(token) <= getPrecedence(operatorStack.peek())) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && operatorStack.peek().equals("(")) {
                    operatorStack.pop();
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses in expression.");
                }
            } else {
                throw new IllegalArgumentException("Invalid token in expression: " + token);
            }
            previousToken = token;
        }

        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek().equals("(")) {
                throw new IllegalArgumentException("Mismatched parentheses in expression.");
            }
            outputQueue.add(operatorStack.pop());
        }
        return outputQueue;
    }

    private Node buildTreeFromPostfix(Queue<String> postfixExpression) {
        Stack<Node> nodeStack = new Stack<>();

        while (!postfixExpression.isEmpty()) {
            String token = postfixExpression.poll();

            if (isOperator(token)) {
                if (nodeStack.size() < 2) {
                    throw new IllegalArgumentException("Invalid postfix expression: not enough operands for operator " + token);
                }
                Node rightOperand = nodeStack.pop();
                Node leftOperand = nodeStack.pop();
                Node operatorNode = new Node(token);
                operatorNode.left = leftOperand;
                operatorNode.right = rightOperand;
                nodeStack.push(operatorNode);
            } else {
                nodeStack.push(new Node(token));
            }
        }

        if (nodeStack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix expression: too many operands or operators.");
        }
        return nodeStack.pop();
    }

    public void buildTree(String infixExpression) {
        Queue<String> postfix = infixToPostfix(infixExpression);
        this.root = buildTreeFromPostfix(postfix);
    }

    private double evaluate(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot evaluate an empty node.");
        }

        if (!isOperator(node.value)) {
            try {
                return Double.parseDouble(node.value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid operand: " + node.value);
            }
        } else {
            double leftValue = evaluate(node.left);
            double rightValue = evaluate(node.right);

            switch (node.value) {
                case "+":
                    return leftValue + rightValue;
                case "-":
                    return leftValue - rightValue;
                case "*":
                    return leftValue * rightValue;
                case "/":
                    if (rightValue == 0) {
                        throw new ArithmeticException("Division by zero.");
                    }
                    return leftValue / rightValue;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + node.value);
            }
        }
    }

    public double evaluate() {
        if (root == null) {
            throw new IllegalStateException("Expression tree is empty. Build the tree first.");
        }
        return evaluate(this.root);
    }
}