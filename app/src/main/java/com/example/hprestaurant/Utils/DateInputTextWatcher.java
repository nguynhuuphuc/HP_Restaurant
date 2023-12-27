package com.example.hprestaurant.Utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class DateInputTextWatcher implements TextWatcher {
    private EditText editText;
    private String current = "";
    private String ddmmyyyy = "DDMMYYYY"; // Định dạng ngày tháng năm mong muốn, có thể thay đổi tùy ý

    public DateInputTextWatcher(EditText editText) {
        this.editText = editText;
        this.editText.addTextChangedListener(this);
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (!charSequence.toString().equals(current)) {
            String clean = charSequence.toString().replaceAll("[^\\d.]", "");
            String cleanC = current.replaceAll("[^\\d.]", "");

            int cl = clean.length();
            int sel = cl;
            for (int i = 2; i <= cl && i < 6; i += 2) {
                sel++;
            }
            // Fix for pressing delete next to a forward slash
            if (clean.equals(cleanC)) sel--;

            if (cl < 8) {
                clean = clean + ddmmyyyy.substring(cl);
            } else {
                // This part makes sure that when we finish entering numbers, the date is correct
                int day = Integer.parseInt(clean.substring(0, 2));
                int mon = Integer.parseInt(clean.substring(2, 4));
                int year = Integer.parseInt(clean.substring(4, 8));

                mon = mon < 1 ? 1 : Math.min(mon, 12);
                // Cho phép năm nhập từ 1900 đến 2100, bạn có thể thay đổi nếu cần
                year = Math.min(year, 2100);

                clean = String.format("%02d%02d%04d", day, mon, year);
            }

            clean = String.format("%s/%s/%s", clean.substring(0, 2),
                    clean.substring(2, 4),
                    clean.substring(4, 8));

            sel = sel < 0 ? 0 : sel;
            current = clean;
            editText.setText(current);
            editText.setSelection(Math.min(sel, current.length()));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
