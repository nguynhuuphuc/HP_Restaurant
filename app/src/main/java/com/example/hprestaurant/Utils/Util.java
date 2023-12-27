package com.example.hprestaurant.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;

import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.R;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
    public static final String HOST = "192.168.1.205";
    public static final String RESTAURANT_PHONE = "0343026233";
    public static final String WEBSOCKET_URL = "ws://" + HOST+":3000";
    private static BitmapDrawable bitmapDrawable;

    public static BitmapDrawable getBitmapDrawable() {
        return bitmapDrawable;
    }

    public static void setBitmapDrawable(BitmapDrawable bitmapDrawable) {
        Util.bitmapDrawable = bitmapDrawable;
    }

    private  IKeyBoardListener iKeyBoardListener;
    public interface IKeyBoardListener{
        void onHidden();
        void onVisible();
    }
    public static Date DateParse(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            // Parse the date string into a LocalDate
            LocalDate parsedDate = LocalDate.parse(dateString, formatter);
            Instant instant = parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error parsing date string.");
            return null;
        }
    }
    public static String DayFormatting(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    public static String DayTimeFormatting(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String TimeFormatting(Date date){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return  timeFormat.format(date);
    }

    public static View.OnTouchListener ShowOrHidePass(final EditText editText){
        String hint = editText.getHint().toString();

        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int DRAWABLE_RIGHT = 2;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Người dùng nhấn vào biểu tượng
                        if (editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                            // Nếu đang hiển thị mật khẩu, chuyển sang ẩn mật khẩu
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_off_24, 0);

                        } else {
                            // Ngược lại, chuyển sang hiển thị mật khẩu
                            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_24, 0);

                        }
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static String dateTimeInMessageFormatting(Date date){
        // Lấy ngày và giờ hiện tại
        Date currentDate = new Date();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        // Ngày và giờ cần định dạng
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);

        // Tạo đối tượng SimpleDateFormat để định dạng kiểu ngày và giờ
        SimpleDateFormat dateFormat;
        if (currentCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR)
                && currentCalendar.get(Calendar.MONTH) == targetCalendar.get(Calendar.MONTH)
                && currentCalendar.get(Calendar.DAY_OF_MONTH) == targetCalendar.get(Calendar.DAY_OF_MONTH)) {
            // Nếu là ngày hiện tại, chỉ hiển thị giờ và phút
            dateFormat = new SimpleDateFormat("h:mm a");
        } else {
            // Ngày không trùng khớp, hiển thị ngày và giờ
            dateFormat = new SimpleDateFormat("yy/MM/dd h:mm a");
        }
        return dateFormat.format(date);
    }


    public static View.OnTouchListener ShowOrHidePass(View root,final EditText editText){
        String hint = editText.getHint().toString();

        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int DRAWABLE_RIGHT = 2;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Người dùng nhấn vào biểu tượng
                        if (editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                            // Nếu đang hiển thị mật khẩu, chuyển sang ẩn mật khẩu
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_off_24, 0);

                        } else {
                            // Ngược lại, chuyển sang hiển thị mật khẩu
                            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_24, 0);

                        }
                        root.getViewTreeObserver().addOnGlobalLayoutListener(onKeyBoardListener(root, new IKeyBoardListener() {
                            @Override
                            public void onHidden() {
                                editText.clearFocus();
                            }

                            @Override
                            public void onVisible() {

                            }
                        }));


                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static ViewTreeObserver.OnGlobalLayoutListener onKeyBoardListener(View root,IKeyBoardListener iKeyBoardListener){
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                root.getWindowVisibleDisplayFrame(r);
                int screenHeight =  root.getHeight();

                // Calculate the height of the keyboard
                int keypadHeight = screenHeight - r.bottom;

                // If the height of the keyboard is less than a certain threshold, it's considered hidden
                boolean isKeyboardVisible = keypadHeight > screenHeight * 0.15; // Adjust this threshold as needed

                if (isKeyboardVisible) {
                    // The keyboard is currently visible
                    // Handle the event here
                    iKeyBoardListener.onVisible();

                } else {
                    // The keyboard is currently hidden
                    // Handle the event here
                    iKeyBoardListener.onHidden();
                }
            }
        };
    }

    public static void customStatusBar(Context context,Window window, BitmapDrawable bitmapDrawable){
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Palette.from(bitmap).generate(palette -> {
                // Lấy màu chính từ Palette
                assert palette != null;
                int statusBarColor = palette.getDominantColor(ContextCompat.getColor(context, android.R.color.primary_text_dark));
                // Đặt màu nền cho thanh trạng thái
                window.setStatusBarColor(statusBarColor);
            });
        }
    }
    public static void customStatusBar(Context context,Window window){
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Palette.from(bitmap).generate(palette -> {
                // Lấy màu chính từ Palette
                assert palette != null;
                int statusBarColor = palette.getDominantColor(ContextCompat.getColor(context, android.R.color.primary_text_dark));
                // Đặt màu nền cho thanh trạng thái
                window.setStatusBarColor(statusBarColor);
            });
        }
    }

    public static String getDayOfWeek(LocalDate date){
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        switch (dayOfWeek.getValue()){
            case 1:
                return "T2";
            case 2:
                return "T3";
            case 3:
                return "T4";
            case 4:
                return "T5";
            case 5:
                return "T6";
            case 6:
                return "T7";
            case 7:
                return "CN";
        }
        return "";
    }

    public static void sharedReferencesSaving(Context context){
        if(!MySharedPreferences.isExistUserUid(context) || !MySharedPreferences.getUserUid(context).equals(Auth.getUserUid())){
            MySharedPreferences.saveUserUid(context,Auth.getUserUid());
        }
        if(!MySharedPreferences.isExistPhoneNumber(context) || !MySharedPreferences.getPhone(context).equals(Auth.getPhoneNumber())){
            MySharedPreferences.savePhone(context,Auth.getPhoneNumber());
        }
    }

    public static void sharedReferencesRemoving(Context context){
        MySharedPreferences.saveUserUid(context,null);
    }

    public static void changeReservationAdding(List<ReservationModel> changeReservations, List<ReservationModel> values){
        if(changeReservations == null ) changeReservations = new ArrayList<>();
        if(changeReservations.isEmpty()){
            changeReservations.addAll(values);
            return;
        }
        Map<Integer, ReservationModel> map = new HashMap<>();

        // Tạo một map từ array1 để dễ dàng kiểm tra sự tồn tại
        for (ReservationModel obj : changeReservations) {
            map.put(obj.getId(), obj);
        }

        // Duyệt qua array2 và ghi đè hoặc thêm mới vào array1
        for (ReservationModel obj : values) {
            if (map.containsKey(obj.getId())) {
                // Ghi đè nếu tồn tại
                ReservationModel existingObject = map.get(obj.getId());
                for(ReservationModel model : changeReservations){
                    if(model.getId() == existingObject.getId()){
                        changeReservations.set(changeReservations.indexOf(model),existingObject);
                        break;
                    }
                }
            } else {
                // Thêm mới nếu không tồn tại
                changeReservations.add(obj);
            }
        }
    }

}
