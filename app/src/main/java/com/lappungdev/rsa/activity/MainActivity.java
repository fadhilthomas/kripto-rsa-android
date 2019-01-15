package com.lappungdev.rsa.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lappungdev.rsa.R;
import com.rustamg.filedialogs.FileDialog;
import com.rustamg.filedialogs.OpenFileDialog;
import com.rustamg.filedialogs.SaveFileDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends AppCompatActivity implements FileDialog.OnFileSelectedListener{

    private TextView tvJudul;
    private TextView tvJudulHasil;
    private TextView tvInput;
    private TextView tvInputDesk;
    private TextView tvWaktuProses;
    private EditText etPublik;
    private EditText etPrivat;
    private EditText etInput;
    private EditText etHasil;
    private Button btChange, btProses;
    private String mode = "Enkripsi";
    private String publicKey = "";
    private String privateKey = "";
    private String input = "";
    private Dialog alertDialog;
    private String hasil = "";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvJudul = findViewById(R.id.tvJudul);
        tvInput = findViewById(R.id.tvInputText);
        etPrivat = findViewById(R.id.etPrivat);
        etPublik = findViewById(R.id.etPublik);
        btChange = findViewById(R.id.btChange);
        etInput = findViewById(R.id.etInput);
        checkPermission(this);
        setContentView(R.layout.activity_main);
    }

    public void change(View view) {
        tvInput = findViewById(R.id.tvInputText);
        tvInputDesk = findViewById(R.id.tvInputDeskText);
        tvJudul = findViewById(R.id.tvJudul);
        tvInput = findViewById(R.id.tvInputText);
        btChange = findViewById(R.id.btChange);
        if(tvJudul.getText().toString().contains("Enkripsi")){
            mode = "Dekripsi";
            tvInput.setText("");
            tvInputDesk.setText("Teks yang telah dienkripsi");
            tvJudul.setText(R.string.dekripsi);
            tvInput.setText(R.string.cipher);
            btChange.setText(R.string.enkripsi);
        }else{
            tvInput.setText("");
            tvInputDesk.setText("Teks berisi informasi yang akan dienkripsi");
            mode = "Enkripsi";
            tvJudul.setText(R.string.enkripsi);
            tvInput.setText(R.string.plain);
            btChange.setText(R.string.dekripsi);
        }
    }

    public void buatKunci(View view) {
        etPrivat = findViewById(R.id.etPrivat);
        etPublik = findViewById(R.id.etPublik);
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048, new SecureRandom());
            KeyPair kp = kpg.generateKeyPair();

            privateKey = "-----BEGIN PRIVATE KEY-----";
            privateKey += Base64.encodeToString(kp.getPrivate().getEncoded(), Base64.DEFAULT);
            privateKey += "-----END PRIVATE KEY-----";
            publicKey = "-----BEGIN PUBLIC KEY-----";
            publicKey += Base64.encodeToString(kp.getPublic().getEncoded(),Base64.DEFAULT);
            publicKey += "-----END PUBLIC KEY-----";

            etPublik.setText(publicKey);
            etPrivat.setText(privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.encodeToString(cipherText, Base64.DEFAULT);
    }

    private static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] bytes = Base64.decode(cipherText, Base64.DEFAULT);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    public void proses(View view) {
        try {
            String result;
            etInput = findViewById(R.id.etInput);
            etPrivat = findViewById(R.id.etPrivat);
            etPublik = findViewById(R.id.etPublik);
            input = etInput.getText().toString();
            btChange = findViewById(R.id.btChange);
            btProses = findViewById(R.id.btProses);

            if(isBase64(input) && mode.contains("Enkripsi")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                alertDialogBuilder
                        .setMessage("Pesan yang kamu pilih terdeteksi sebagai cipher teks, apakah kamu mau beralih ke mode Dekripsi?")
                        .setCancelable(true)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                btChange.callOnClick();
                                btProses.callOnClick();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                final AlertDialog alertDialog2 = alertDialogBuilder.create();
                alertDialog2.show();
                TextView pesan = alertDialog2.findViewById(android.R.id.message);
                Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/font.ttf");
                pesan.setTextSize(15);
                pesan.setTypeface(custom_font);
                Button a = alertDialog2.getButton(DialogInterface.BUTTON_NEGATIVE);
                a.setTextColor(getResources().getColor(R.color.colorKey));
                Button b = alertDialog2.getButton(DialogInterface.BUTTON_POSITIVE);
                b.setTextColor(getResources().getColor(R.color.colorSc));
                b.setTypeface(b.getTypeface(), Typeface.BOLD);
            }else {
                long startTime = System.currentTimeMillis();
                int jumlahKarakter = 0;
                long endTime = 0;
                if (mode.contains("Enkripsi")) {
                    result = encrypt(input, restorePublic(etPublik.getText().toString()));
                    jumlahKarakter = etInput.getText().length();
                    endTime = System.currentTimeMillis();
                } else {
                    result = decrypt(input, restorePrivate(etPrivat.getText().toString()));
                    jumlahKarakter = result.length();
                    endTime = System.currentTimeMillis();
                }
                alertDialog = new Dialog(this);
                alertDialog.setContentView(R.layout.result);
                etHasil = alertDialog.findViewById(R.id.etHasil);
                tvJudulHasil = alertDialog.findViewById(R.id.tvJudulHasil);
                tvWaktuProses = alertDialog.findViewById(R.id.tvWaktuProses);
                tvJudulHasil.setText("Hasil " + mode);
                long timeProses = (endTime - startTime);
                tvWaktuProses.setText(mode + " " + jumlahKarakter +" karakter dalam waktu " + timeProses + " ms");
                Button btSalin = alertDialog.findViewById(R.id.btSalin);
                Button btSimpanHasil = alertDialog.findViewById(R.id.btSimpanHasil);
                btSalin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, etHasil.getText().toString());
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Kirim"));
                        alertDialog.dismiss();
                    }
                });

                btSimpanHasil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hasil = etHasil.getText().toString();
                        showFileDialog(new SaveFileDialog(), SaveFileDialog.class.getName() + ":" + "Hasil");
                        alertDialog.dismiss();
                    }
                });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                etHasil.setText(result);
                alertDialog.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private PublicKey restorePublic(String publicK){
        PublicKey pubKey = null;
        try {
            publicK = publicK.replaceAll("-----BEGIN PUBLIC KEY-----", "").replaceAll("-----END PUBLIC KEY-----", "");
            byte[] publicBytes = Base64.decode(publicK, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            pubKey = keyFactory.generatePublic(keySpec);
        }catch (Exception e){
            e.printStackTrace();
        }
        return pubKey;
    }

    private PrivateKey restorePrivate(String privateK){
        PrivateKey privKey = null;
        try {
            privateK = privateK.replaceAll("-----BEGIN PRIVATE KEY-----", "").replaceAll("-----END PRIVATE KEY-----", "");
            byte[] privateBytes = Base64.decode(privateK, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privKey = keyFactory.generatePrivate(keySpec);
        }catch (Exception e){
            e.printStackTrace();
        }
        return privKey;
    }

    public void pilihPublik(View view) {
        showFileDialog(new OpenFileDialog(), OpenFileDialog.class.getName()+":"+"Public");
    }

    private void showFileDialog(FileDialog dialog, String tag) {
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogPilih);
        dialog.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void onFileSelected(FileDialog dialog, File file) {
        etPrivat = findViewById(R.id.etPrivat);
        etPublik = findViewById(R.id.etPublik);
        etInput = findViewById(R.id.etInput);
        try {
            if(dialog.getTag().contains(OpenFileDialog.class.getName())) {
                Snackbar.make(findViewById(android.R.id.content), "File berhasil dipilih", Snackbar.LENGTH_LONG).show();
                String input = FileUtils.readFileToString(file.getAbsoluteFile());
                if (dialog.getTag().contains("Public")) {
                    etPublik.setText(input);
                } else if (dialog.getTag().contains("Private")) {
                    etPrivat.setText(input);
                } else if (dialog.getTag().contains("Input")) {
                    etInput.setText(input);
                }
            }else if(dialog.getTag().contains(SaveFileDialog.class.getName())) {
                try (FileOutputStream stream = new FileOutputStream(file.getAbsoluteFile())) {
                    if (dialog.getTag().contains("Public")) {
                        stream.write(etPublik.getText().toString().getBytes());
                        stream.close();
                        Snackbar.make(findViewById(android.R.id.content), "File kunci publik berhasil disimpan di: "+file.getAbsoluteFile(), Snackbar.LENGTH_LONG).show();
                    } else if (dialog.getTag().contains("Private")) {
                        stream.write(etPrivat.getText().toString().getBytes());
                        stream.close();
                        Snackbar.make(findViewById(android.R.id.content), "File kunci privat berhasil disimpan di: "+file.getAbsoluteFile(), Snackbar.LENGTH_LONG).show();
                    } else if (dialog.getTag().contains("Hasil")) {
                        stream.write(hasil.getBytes());
                        stream.close();
                        Snackbar.make(findViewById(android.R.id.content), "File berhasil disimpan di: "+file.getAbsoluteFile(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void pilihPrivat(View view) {
        showFileDialog(new OpenFileDialog(), OpenFileDialog.class.getName()+":"+"Private");
    }

    public void simpanPrivat(View view) {
        showFileDialog(new SaveFileDialog(), SaveFileDialog.class.getName()+":"+"Private");
    }

    public void simpanPublik(View view) {
        showFileDialog(new SaveFileDialog(), SaveFileDialog.class.getName()+":"+"Public");
    }

    public void pilihInput(View view) {
        showFileDialog(new OpenFileDialog(), OpenFileDialog.class.getName()+":"+"Input");
    }

    private static boolean isBase64(String str) {
        return str.contains("==") || str.contains("+") || str.contains("/");
    }

    public void tentang(View view) {
        alertDialog = new Dialog(this);
        alertDialog.setContentView(R.layout.about);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions((Activity) context,new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
            }
        }
    }

    public void showDialog(final String msg, final Context context, final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions((Activity) context, new String[] { permission }, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Izin untuk membuka galeri ditolak.", Snackbar.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
