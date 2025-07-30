package com.example.calculator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calculator.databinding.ActivityMainBinding;
import com.example.calculator.calculator.ExpressionTree;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView inputTextView;
    private TextView resultTextView;
    private HorizontalScrollView inputScrollView;
    private HorizontalScrollView resultScrollView;
    private final StringBuilder currentExpression = new StringBuilder();
    private boolean lastInputWasEquals = false;

    private static final float INPUT_TEXT_MAX_SIZE = 48f;
    private static final float INPUT_TEXT_MIN_SIZE = 24f;
    private static final float RESULT_TEXT_MAX_SIZE = 42f;
    private static final float RESULT_TEXT_MIN_SIZE = 16f;

    private final DecimalFormat displayFormat = new DecimalFormat("#,##0.##########");
    private final Pattern numberPattern = Pattern.compile("(\\d+\\.?\\d*|\\.\\d+)");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inputTextView = binding.inputTextView;
        resultTextView = binding.resultTextView;
        inputScrollView = binding.inputScrollView;
        resultScrollView = binding.resultScrollView;

        inputTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, INPUT_TEXT_MAX_SIZE);
        resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, RESULT_TEXT_MAX_SIZE);

        updateInputDisplay("");
        updateResultDisplay("0");

        setupTextSizeAdjustment(inputTextView, inputScrollView, INPUT_TEXT_MAX_SIZE, INPUT_TEXT_MIN_SIZE);
        setupTextSizeAdjustment(resultTextView, resultScrollView, RESULT_TEXT_MAX_SIZE, RESULT_TEXT_MIN_SIZE);
    }

    public void onNumberOrDecimalClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        if (lastInputWasEquals) {
            currentExpression.setLength(0);
            updateResultDisplay("0");
            lastInputWasEquals = false;
        }

        if (buttonText.equals(".")) {
            if (currentExpression.length() == 0 || isOperator(currentExpression.substring(currentExpression.length() - 1)) || currentExpression.substring(currentExpression.length() - 1).equals(".")) {
                currentExpression.append("0");
            }
            if (currentExpression.toString().matches(".*\\d+\\.$")) {
                return;
            }
        }

        if (currentExpression.toString().equals("0") && !buttonText.equals(".")) {
            currentExpression.setLength(0);
        }

        currentExpression.append(buttonText);
        updateInputDisplay(currentExpression.toString());
        evaluateExpression();
    }

    public void onOperatorClick(View view) {
        Button button = (Button) view;
        String operator = button.getText().toString();

        if (currentExpression.length() == 0) {
            if (operator.equals("-")) {
                currentExpression.append(operator);
                updateInputDisplay(currentExpression.toString());
                updateResultDisplay("");
            }
            return;
        }

        if (lastInputWasEquals) {
            currentExpression.setLength(0);
            String previousResultInInput = inputTextView.getText().toString();
            if (!previousResultInInput.equals("Error") && !previousResultInInput.isEmpty()) {
                currentExpression.append(previousResultInInput.replace(",", ""));
            } else {
                currentExpression.append("0");
            }
            lastInputWasEquals = false;
        }

        char lastChar = currentExpression.charAt(currentExpression.length() - 1);
        boolean lastCharIsOperator = isOperator(String.valueOf(lastChar));

        if (lastCharIsOperator) {
            if (operator.equals("-")) {
                currentExpression.append(operator);
            } else {
                currentExpression.setLength(currentExpression.length() - 1);
                currentExpression.append(operator);
            }
        } else {
            currentExpression.append(operator);
        }
        updateInputDisplay(currentExpression.toString());
        updateResultDisplay("");
    }

    public void onClearClick(View view) {
        currentExpression.setLength(0);
        updateInputDisplay("");
        updateResultDisplay("0");
        lastInputWasEquals = false;
        inputTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, INPUT_TEXT_MAX_SIZE);
        resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, RESULT_TEXT_MAX_SIZE);
    }

    public void onDeleteClick(View view) {
        if (currentExpression.length() > 0) {
            currentExpression.setLength(currentExpression.length() - 1);
            updateInputDisplay(currentExpression.toString());
            if (currentExpression.length() == 0) {
                updateResultDisplay("0");
            } else {
                if (!isOperator(currentExpression.substring(currentExpression.length() - 1))) {
                    evaluateExpression();
                } else {
                    updateResultDisplay("");
                }
            }
        } else {
            updateInputDisplay("");
            updateResultDisplay("0");
        }
        lastInputWasEquals = false;
    }

    public void onEqualsClick(View view) {
        if (currentExpression.length() == 0) {
            return;
        }

        evaluateExpression();
        String finalResult = resultTextView.getText().toString();

        if (finalResult.equals("Error") || finalResult.isEmpty()) {
            currentExpression.setLength(0);
            updateInputDisplay("");
            updateResultDisplay("0");
            lastInputWasEquals = false;
            return;
        }

        currentExpression.setLength(0);
        currentExpression.append(finalResult.replace(",", ""));
        updateInputDisplay(currentExpression.toString());

        updateResultDisplay("0");

        lastInputWasEquals = true;
    }

    private void evaluateExpression() {
        String expressionToEvaluate = currentExpression.toString();

        if (!expressionToEvaluate.isEmpty() && isOperator(expressionToEvaluate.substring(expressionToEvaluate.length() - 1))) {
            updateResultDisplay("");
            return;
        }

        if (expressionToEvaluate.isEmpty()) {
            updateResultDisplay("0");
            return;
        }

        try {
            ExpressionTree calculator = new ExpressionTree();
            calculator.buildTree(expressionToEvaluate);
            double result = calculator.evaluate();

            String formattedResult = displayFormat.format(result);
            updateResultDisplay(formattedResult);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException e) {
            updateResultDisplay("Error");
        }
    }

    private void updateInputDisplay(String text) {
        inputTextView.setText(formatExpressionForDisplay(text));
        inputScrollView.post(() -> inputScrollView.fullScroll(View.FOCUS_RIGHT));
    }

    private String formatExpressionForDisplay(String expression) {
        if (expression.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        Matcher matcher = numberPattern.matcher(expression);
        int lastIndex = 0;

        while (matcher.find()) {
            formatted.append(expression.substring(lastIndex, matcher.start()));

            String numberStr = matcher.group();
            try {
                boolean endsWithDot = numberStr.endsWith(".");
                if (endsWithDot) {
                    numberStr = numberStr.substring(0, numberStr.length() - 1);
                }

                if (numberStr.startsWith(".") && numberStr.length() > 1) {
                    formatted.append("0");
                }

                double num = Double.parseDouble(numberStr);
                formatted.append(displayFormat.format(num));

                if (endsWithDot) {
                    formatted.append(".");
                }

            } catch (NumberFormatException e) {
                formatted.append(numberStr);
            }
            lastIndex = matcher.end();
        }
        formatted.append(expression.substring(lastIndex));
        return formatted.toString();
    }

    private void updateResultDisplay(String text) {
        resultTextView.setText(text);
        resultScrollView.post(() -> resultScrollView.fullScroll(View.FOCUS_RIGHT));
    }

    private boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/");
    }

    private void setupTextSizeAdjustment(final TextView textView, final HorizontalScrollView scrollView, final float maxSize, final float minSize) {
        textView.getViewTreeObserver().addOnGlobalLayoutListener(() -> adjustTextSize(textView, scrollView, maxSize, minSize));

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adjustTextSize(textView, scrollView, maxSize, minSize);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void adjustTextSize(TextView textView, HorizontalScrollView scrollView, float initialSize, float minSize) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, initialSize);
        float currentSize = initialSize;

        int availableWidth = scrollView.getWidth() - scrollView.getPaddingLeft() - scrollView.getPaddingRight();

        if (availableWidth <= 0) {
            return;
        }

        float textWidth = textView.getPaint().measureText(textView.getText().toString());

        while (textWidth > availableWidth && currentSize > minSize) {
            currentSize -= 1f;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
            textWidth = textView.getPaint().measureText(textView.getText().toString());
        }

        if (textWidth <= availableWidth && currentSize < initialSize) {
            float nextSize = currentSize + 1f;
            if (nextSize <= initialSize) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, nextSize);
                textWidth = textView.getPaint().measureText(textView.getText().toString());
                if (textWidth > availableWidth) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);
                }
            }
        }
    }
}