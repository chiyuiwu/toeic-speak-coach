package com.robert.toeicspeak;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST = 200;
    private static final int AUDIO_PERMISSION_REQUEST = 201;
    private static final int NAVY = Color.rgb(16, 28, 61);
    private static final int INK = Color.rgb(31, 42, 68);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int ORANGE = Color.rgb(255, 107, 61);
    private static final int PALE = Color.rgb(243, 246, 252);
    private static final int GREEN = Color.rgb(21, 128, 61);

    private final List<Question> questions = Arrays.asList(
        new Question("The quarterly report must be submitted _____ Friday.",
            new String[]{"at", "by", "during", "since"}, 1,
            "by 表示「不晚於某個期限」，因此 by Friday 最符合句意。"),
        new Question("Ms. Chen is responsible for _____ the client presentation.",
            new String[]{"prepare", "prepared", "preparing", "preparation"}, 2,
            "介系詞 for 後面要接名詞或動名詞，因此使用 preparing。"),
        new Question("The new software is easier to use _____ the previous version.",
            new String[]{"than", "then", "that", "from"}, 0,
            "比較級 easier 後面使用 than。"),
        new Question("Please contact the front desk if you require _____ assistance.",
            new String[]{"add", "addition", "additional", "additionally"}, 2,
            "assistance 是名詞，前面需要形容詞 additional 修飾。"),
        new Question("Neither the manager nor the employees _____ aware of the change.",
            new String[]{"was", "were", "be", "has"}, 1,
            "neither...nor 的動詞通常與較近的主詞一致；employees 是複數，所以用 were。")
    );

    private final String[] speakingPrompts = {
        "Describe a challenge you faced at work and explain how you solved it.",
        "A customer calls to complain about a delayed delivery. Respond politely and offer a solution.",
        "Do you prefer working independently or as part of a team? Give reasons and examples."
    };

    private LinearLayout content;
    private Button practiceTab;
    private Button speakingTab;
    private int questionIndex = 0;
    private int correctCount = 0;
    private int promptIndex = 0;
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showShell();
        showToeic();
    }

    private void showShell() {
        LinearLayout root = vertical(NAVY);
        root.setPadding(dp(18), dp(18), dp(18), 0);

        TextView brand = text("TOEIC  SPEAK COACH", 13, Color.rgb(255, 184, 103), true);
        brand.setLetterSpacing(.12f);
        root.addView(brand);
        TextView title = text("每天進步一點，開口更有自信", 24, Color.WHITE, true);
        title.setPadding(0, dp(7), 0, dp(16));
        root.addView(title);

        LinearLayout tabs = horizontal(Color.TRANSPARENT);
        practiceTab = tab("TOEIC 練習");
        speakingTab = tab("口說教練");
        tabs.addView(practiceTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        tabs.addView(speakingTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        root.addView(tabs);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = vertical(PALE);
        content.setPadding(dp(16), dp(18), dp(16), dp(30));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        practiceTab.setOnClickListener(v -> showToeic());
        speakingTab.setOnClickListener(v -> showSpeaking());
    }

    private void showToeic() {
        setActiveTab(practiceTab, speakingTab);
        content.removeAllViews();
        Question q = questions.get(questionIndex);

        LinearLayout status = horizontal(Color.TRANSPARENT);
        status.setGravity(Gravity.CENTER_VERTICAL);
        TextView count = text("題目 " + (questionIndex + 1) + " / " + questions.size(), 15, MUTED, true);
        TextView score = text("答對 " + correctCount + " 題", 15, ORANGE, true);
        status.addView(count, new LinearLayout.LayoutParams(0, dp(34), 1));
        status.addView(score);
        content.addView(status);

        LinearLayout card = card();
        TextView kind = text("PART 5 · INCOMPLETE SENTENCES", 12, ORANGE, true);
        kind.setLetterSpacing(.08f);
        card.addView(kind);
        TextView question = text(q.text, 21, INK, true);
        question.setLineSpacing(0, 1.2f);
        question.setPadding(0, dp(14), 0, dp(16));
        card.addView(question);

        for (int i = 0; i < q.options.length; i++) {
            final int selected = i;
            Button option = button(((char) ('A' + i)) + "   " + q.options[i], Color.WHITE, INK);
            option.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
            lp.setMargins(0, dp(6), 0, dp(6));
            card.addView(option, lp);
            option.setOnClickListener(v -> answerQuestion(selected, q));
        }
        content.addView(card);
    }

    private void answerQuestion(int selected, Question q) {
        if (answered) return;
        answered = true;
        boolean correct = selected == q.answer;
        if (correct) correctCount++;

        LinearLayout feedback = card();
        feedback.setBackgroundColor(correct ? Color.rgb(232, 248, 238) : Color.rgb(255, 239, 235));
        feedback.addView(text(correct ? "答對了！" : "再留意一下", 19, correct ? GREEN : Color.rgb(190, 55, 35), true));
        TextView body = text("正確答案：" + (char) ('A' + q.answer) + "  " + q.options[q.answer] + "\n\n" + q.explanation,
            16, INK, false);
        body.setLineSpacing(0, 1.25f);
        body.setPadding(0, dp(10), 0, dp(14));
        feedback.addView(body);
        Button next = button(questionIndex == questions.size() - 1 ? "重新練習" : "下一題", ORANGE, Color.WHITE);
        feedback.addView(next, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        content.addView(feedback);
        next.setOnClickListener(v -> {
            questionIndex++;
            if (questionIndex >= questions.size()) { questionIndex = 0; correctCount = 0; }
            answered = false;
            showToeic();
        });
    }

    private void showSpeaking() {
        setActiveTab(speakingTab, practiceTab);
        content.removeAllViews();

        TextView meta = text("TOEIC SPEAKING · EXPRESS AN OPINION", 12, ORANGE, true);
        meta.setLetterSpacing(.06f);
        content.addView(meta);

        LinearLayout prompt = card();
        prompt.addView(text("請用英文回答", 16, MUTED, true));
        TextView question = text(speakingPrompts[promptIndex], 22, INK, true);
        question.setLineSpacing(0, 1.18f);
        question.setPadding(0, dp(12), 0, dp(18));
        prompt.addView(question);
        TextView tip = text("建議作答 30–45 秒：先說立場，再提供理由與例子。", 15, MUTED, false);
        prompt.addView(tip);
        content.addView(prompt);

        Button record = button("●  開始回答", ORANGE, Color.WHITE);
        LinearLayout.LayoutParams recordLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        recordLp.setMargins(0, dp(14), 0, dp(10));
        content.addView(record, recordLp);
        record.setOnClickListener(v -> ensurePermissionAndListen());

        Button nextPrompt = button("換一個題目", Color.WHITE, INK);
        content.addView(nextPrompt, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
        nextPrompt.setOnClickListener(v -> {
            promptIndex = (promptIndex + 1) % speakingPrompts.length;
            showSpeaking();
        });

        LinearLayout info = card();
        info.addView(text("評分重點", 18, INK, true));
        TextView criteria = text("• 回答是否切題且完整\n• 是否使用連接詞組織內容\n• 是否有常見文法問題\n• 語速與贅字是否影響理解", 15, MUTED, false);
        criteria.setLineSpacing(dp(5), 1f);
        criteria.setPadding(0, dp(10), 0, 0);
        info.addView(criteria);
        content.addView(info);
    }

    private void ensurePermissionAndListen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST);
        } else startListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == AUDIO_PERMISSION_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else Toast.makeText(this, "需要麥克風權限才能進行口說練習。", Toast.LENGTH_LONG).show();
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in English");
        try { startActivityForResult(intent, SPEECH_REQUEST); }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, "這台手機沒有可用的語音辨識服務。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) showSpeechFeedback(results.get(0));
        }
    }

    private void showSpeechFeedback(String transcript) {
        LinearLayout result = card();
        result.addView(text("辨識結果", 14, MUTED, true));
        TextView transcriptView = text("“" + transcript + "”", 18, INK, false);
        transcriptView.setLineSpacing(0, 1.2f);
        transcriptView.setPadding(0, dp(8), 0, dp(15));
        result.addView(transcriptView);

        String lower = transcript.toLowerCase(Locale.US).trim();
        int words = lower.isEmpty() ? 0 : lower.split("\\s+").length;
        int fillers = count(lower, " um ") + count(lower, " uh ") + count(lower, " like ");
        int score = Math.min(92, 45 + words * 2);
        if (words < 12) score -= 12;
        if (!containsAny(lower, "because", "for example", "first", "however", "therefore")) score -= 6;
        if (fillers > 1) score -= 5;
        score = Math.max(35, score);

        result.addView(text("口說完整度  " + score + " / 100", 20, ORANGE, true));
        StringBuilder notes = new StringBuilder();
        if (words < 12) notes.append("\n• 回答偏短：補上原因與一個具體例子。\n");
        else notes.append("\n• 回答長度足以表達主要觀點。\n");
        if (!containsAny(lower, "because", "for example", "first", "however", "therefore"))
            notes.append("• 加入 because、for example 或 however，讓結構更清楚。\n");
        else notes.append("• 有使用連接語，內容較容易理解。\n");
        if (lower.matches(".*\\bhe go\\b.*")) notes.append("• 文法：he go 應改為 he goes。\n");
        if (lower.matches(".*\\bi am agree\\b.*")) notes.append("• 文法：I am agree 應改為 I agree。\n");
        if (lower.matches(".*\\bpeople is\\b.*")) notes.append("• 文法：people is 應改為 people are。\n");
        if (fillers > 1) notes.append("• 贅字稍多；停頓比重複 um / uh 更自然。\n");
        notes.append("• 發音提醒：把關鍵字說清楚，句尾不要吞音。\n");

        TextView advice = text(notes.toString().trim(), 15, INK, false);
        advice.setLineSpacing(dp(4), 1f);
        advice.setPadding(0, dp(6), 0, 0);
        result.addView(advice);
        content.addView(result, 0);
        content.post(() -> content.getParent().requestChildFocus(result, result));
    }

    private int count(String text, String needle) {
        int total = 0, pos = 0;
        String padded = " " + text + " ";
        while ((pos = padded.indexOf(needle, pos)) >= 0) { total++; pos += needle.length(); }
        return total;
    }

    private boolean containsAny(String value, String... options) {
        for (String option : options) if (value.contains(option)) return true;
        return false;
    }

    private LinearLayout vertical(int color) {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(color);
        return v;
    }

    private LinearLayout horizontal(int color) {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.HORIZONTAL);
        v.setBackgroundColor(color);
        return v;
    }

    private LinearLayout card() {
        LinearLayout card = vertical(Color.WHITE);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(8));
        card.setLayoutParams(lp);
        card.setElevation(dp(2));
        return card;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private Button button(String value, int bg, int fg) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(16);
        b.setTextColor(fg);
        b.setBackgroundColor(bg);
        b.setAllCaps(false);
        b.setPadding(dp(16), 0, dp(16), 0);
        return b;
    }

    private Button tab(String value) {
        Button b = button(value, Color.TRANSPARENT, Color.WHITE);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return b;
    }

    private void setActiveTab(Button active, Button inactive) {
        active.setTextColor(Color.WHITE);
        active.setBackgroundColor(ORANGE);
        inactive.setTextColor(Color.rgb(190, 200, 225));
        inactive.setBackgroundColor(Color.TRANSPARENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Question {
        final String text;
        final String[] options;
        final int answer;
        final String explanation;
        Question(String text, String[] options, int answer, String explanation) {
            this.text = text; this.options = options; this.answer = answer; this.explanation = explanation;
        }
    }
}
