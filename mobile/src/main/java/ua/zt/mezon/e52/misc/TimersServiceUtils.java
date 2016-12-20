package ua.zt.mezon.e52.misc;

import android.app.Dialog;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by MezM on 30.11.2016.
 */

public class TimersServiceUtils {
    public static final String FILE_EXT_ALLTimersDATA = "alldata";
    public static final String ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/E52Timers";

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void saveTimerImageStringToExternalStorage(String finalstring, String extdata, View v,String fileName) {
        if (isExternalStorageWritable()) {


            File myDir = new File(ROOT);
            myDir.mkdirs();
            //  Random generator = new Random();
            int n = 1;
            // n = generator.nextInt(n);
            Calendar now = Calendar.getInstance();
            String fprefixname;
            if (fileName == null) {
                fprefixname= "store-";
            } else {
                fprefixname=fileName+ "-";
            };
            String fname = fprefixname
                    + n + "-"
                    + Integer.toString(now.get(Calendar.DATE)) + "-"
                    + Integer.toString(now.get(Calendar.MONTH)) + "-"
                    + Integer.toString(now.get(Calendar.YEAR)) + "."
                    + extdata;
            File file = new File(myDir, fname);

            while (file.exists()) {
                n++;
                fname = "store-"
                        + n + "-"
                        + Integer.toString(now.get(Calendar.DATE)) + "-"
                        + Integer.toString(now.get(Calendar.MONTH)) + "-"
                        + Integer.toString(now.get(Calendar.YEAR)) + "."
                        + extdata;
                file = new File(myDir, fname);
            }

            try {
                FileOutputStream out = new FileOutputStream(file);
//            File myFile = new File(Environment.getExternalStorageDirectory().getPath()+"/textfile.txt");


                OutputStreamWriter myOutWriter = new OutputStreamWriter(out);
                myOutWriter.write(finalstring);
                myOutWriter.close();

                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            Toast.makeText(v.getContext(), "External Storage NOT Writable>", Toast.LENGTH_SHORT).show();

        }
    }

    public static String loadTimerImageStringFromExternalStorage(String ftype, View v,final TextListener listener) {


        String[] mFileList;
        final String[] mChosenFile = new String[1];
        Dialog dialog = null;
        File mPath = new File(ROOT);
        final String[] mChosenStringfrmFile = {null};
        if (isExternalStorageWritable()) {
            try {
                mPath.mkdirs();
            } catch (SecurityException e) {
                //Log.e(TAG, "unable to write on the sd card " + e.toString());
            }
            if (mPath.exists()) {
                FilenameFilter filter = new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String filename) {
                        File sel = new File(dir, filename);
                        return filename.contains(ftype) || sel.isDirectory();
                    }

                };
                mFileList = mPath.list(filter);
            } else {
                mFileList = null;
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());


            builder.setTitle("Choose your file");
            if (mFileList == null) {

                return null;

            } else {
                mChosenFile[0] = mFileList[0];
            }
            builder.setSingleChoiceItems(mFileList,0, (dialog1, which) -> {
                mChosenFile[0] = mFileList[which];
                //you can do stuff with the file here too
            });
            builder.setPositiveButton(android.R.string.yes, (dialog1, which) -> {
                // continue with load
                File fl  = new File(ROOT,  mChosenFile[0]);;


                FileInputStream fin = null;
                try {
                    fin = new FileInputStream(fl);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    mChosenStringfrmFile[0] = convertStreamToString(fin);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Make sure you close all streams.
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
              //  Toast.makeText(v.getContext(),  mChosenStringfrmFile[0], Toast.LENGTH_SHORT).show();
                listener.onPositiveResult(mChosenStringfrmFile[0]);
            })
                    .setNegativeButton(android.R.string.no, (dialog1, which) -> {
                        // do nothing
                        mChosenStringfrmFile[0] = null;

                    })
                    .setIcon(android.R.drawable.ic_menu_upload);


            dialog = builder.show();

        } else {

            Toast.makeText(v.getContext(), "External Storage NOT Writable>", Toast.LENGTH_SHORT).show();

        }

        return mChosenStringfrmFile[0];
    }
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
    public static String TimeInMilisToStr(long time) {
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public static int getIdXbyId_alTimersCategories(ArrayList<TimerWorkspace> alTmpTimersCategories, int id) {

        for (int i = 0; i < alTmpTimersCategories.size(); i++) {
            if (alTmpTimersCategories.get(i).id == id) {
                return i;
            }
        }

        return -1;
    }

    public static int getIdXbyId_timersTimes(ArrayList<TimersTime> alTmp, int id) {

        for (int i = 0; i < alTmp.size(); i++) {
            if (alTmp.get(i).id == id) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isValidId_timersTimes(ArrayList<TimersTime> alTmp, int id) {

        for (TimersTime z : alTmp
                ) {
            if (z.id == id) {
                return false;
            }
        }

        return true;
    }

    public static int getIdXbyId_alTimersCategoryInWorkspace(ArrayList<TimersCategoryInWorkspace> alTmp, int id) {

        for (int i = 0; i < alTmp.size(); i++) {
            if (alTmp.get(i).id == id) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isValidId_alTimersCategoryInWorkspace(ArrayList<TimersCategoryInWorkspace> alTmp, int id) {

        for (TimersCategoryInWorkspace z : alTmp
                ) {
            if (z.id == id) {
                return false;
            }
        }

        return true;
    }

    public static String convertTimersCategoryInWorkspace(TimersCategoryInWorkspace tmp) {
        Type listOfConvertObject = new TypeToken<TimersCategoryInWorkspace>() {
        }.getType();
        Gson gson = new Gson();
        return gson.toJson(tmp, listOfConvertObject);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isValidId_alTimersCategories(ArrayList<TimerWorkspace> alTmpTimersCategories, int id) {

        for (TimerWorkspace z : alTmpTimersCategories
                ) {
            if (z.id == id) {
                return false;
            }
        }

        return true;
    }


}
