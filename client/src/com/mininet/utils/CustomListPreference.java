package com.mininet.utils;

import java.sql.Date;
import java.util.Calendar;
import java.util.Set;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;

import com.mininet.MatchPrefActivity;

public class CustomListPreference extends ListPreference {

   public static final String TAG = "CustomListPreference";

   public CustomListPreference(Context context) {
      super(context);
   }

   public CustomListPreference(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   @Override
      protected void onDialogClosed(boolean positiveResult) {
         super.onDialogClosed(positiveResult);
         if (!positiveResult) {
            return;
         }
         if (getValue().equals("2")) {
            final String key = getKey();
            // Gender
            if (key.equals("match_gender")) {
               new AlertDialog.Builder(MatchPrefActivity.getInstance())
                  .setTitle("Select one")
                  .setItems(Utils.genders, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                        setSummary(Utils.genders[id]);
                        Utils.getPreferences().edit().putString(key+"_custom", 
                           Utils.genders[id]).apply();
                        Log.d(TAG, key+"_custom: "+Utils.genders[id]);
                     }
                  }).show();
            }
            // Birthdate
            else if (key.equals("match_birthdate")) {
               Log.d(TAG, "key = match_birthdate");
               final Date birthdate = Utils.getUserProfile().getBirthdate();
               final Calendar c = Calendar.getInstance();
               c.setTime(birthdate);
               new DatePickerDialog(MatchPrefActivity.getInstance(), 
                     new DatePickerDialog.OnDateSetListener() {
                        // when dialog box is closed, below method will be called.
                        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                           c.set(selectedYear, selectedMonth, selectedDay);
                           Date date = new Date(c.getTimeInMillis());
                           setSummary(date.toString());
                           Utils.getPreferences().edit().putString(key+"_custom", 
                              date.toString()).apply();
                        }
                     }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
            else {
               final MultiAutoCompleteTextView editText = new MultiAutoCompleteTextView(MatchPrefActivity.getInstance());
               Set<String> interests = Utils.getInterests();
               editText.setAdapter(
                     new ArrayAdapter<String>(MatchPrefActivity.getInstance(),
                                         android.R.layout.simple_dropdown_item_1line, 
                                         interests.toArray(new String[interests.size()]))
                     );
               editText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
               editText.append(getSummary());
               new AlertDialog.Builder(MatchPrefActivity.getInstance())
                  .setTitle("Enter text")
                  .setView(editText)
                  .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                        setSummary(editText.getText().toString());
                        Utils.getPreferences().edit().putString(key+"_custom", 
                           editText.getText().toString()).apply();
                        Log.d(TAG, key+"_custom: "+editText.getText());
                     }
                  })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                  }
               }).show();
            }
         }
         else {
            setSummary(getEntry());
         }
      }

}
