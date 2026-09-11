package mobile.backend.java;

import android.app.Activity;
import android.content.Intent;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import org.haxe.extension.Extension;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import android.util.Log;
import java.net.HttpURLConnection;

/**
 * * @Authors LumiCoder, (FNF BR) and StarNova, (Cream.BR)
 * 
 * @version: 0.1.6
 **/
public class FileUtils extends Extension {

    private static final int CREATE_FILE_CODE = 1024;
    private static final int PICK_FILE_CODE = 1025;
    private static boolean downloadSuccess = false;
    private static boolean isFinished = false;
    private static String contentToSave = "";

    public static org.haxe.lime.HaxeObject callbackObject;

    public static void saveFile(final String fileName, final String data) {
        if (data == null || data.isEmpty())
            return;

        contentToSave = data;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, fileName);

                    if (Extension.mainActivity != null) {
                        Extension.mainActivity.startActivityForResult(intent, CREATE_FILE_CODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void browseFiles(final String mimeType, final org.haxe.lime.HaxeObject callback) {
        callbackObject = callback;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(mimeType != null ? mimeType : "*/*");

                    if (Extension.mainActivity != null) {
                        Extension.mainActivity.startActivityForResult(intent, PICK_FILE_CODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_FILE_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    writeFileToUri(uri);
                }
            } else {
                contentToSave = "";
            }
            return true;
        }

        if (requestCode == PICK_FILE_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null && callbackObject != null) {
                    readBytesFromUri(uri);
                }
            }
            return true;
        }

        return false;
    }

    private static void writeFileToUri(final Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream fileOutputStream = Extension.mainActivity.getContentResolver().openOutputStream(uri);

                    if (fileOutputStream != null) {
                        byte[] bytesToWrite = contentToSave.getBytes("UTF-8");

                        fileOutputStream.write(bytesToWrite);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        contentToSave = "";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void readBytesFromUri(final Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = Extension.mainActivity.getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }

                    byte[] fileBytes = byteBuffer.toByteArray();
                    String fileName = "file.json";

                    callbackObject.call("onFileSelected", new Object[] { fileBytes, fileName });

                    inputStream.close();
                    byteBuffer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static boolean downloadFile(String fileURL, String savePath) {
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0");
            httpConn.setInstanceFollowRedirects(true);

            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(savePath);

                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}