package com.lovejoy777.rroandlayersmanager.activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OverlayDetailActivity extends AppCompatActivity implements AsyncResponse {

    private CheckBox dontShowAgain;
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private String choosedStyle = "";
    private Layer layer;
    private Switch installEverything;
    private FloatingActionButton installationFAB;
    private CoordinatorLayout cordLayout;
    private LoadDrawables imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_plugindetail);

        cordLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        receiveIntent();

        cordLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                loadBackdrop();
            }
        });


        createLayouts();

        Log.d("Colors", String.valueOf(layer.getColors()));

    }

    private boolean isAnyCheckboxEnabled() {

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }


    private void refreshFab() {

        if (isAnyCheckboxEnabled()) {
            installationFAB.show();
        } else {
            installationFAB.hide();
        }

    }

    private void loadOverlayCardviews() {
        //sort cardviews by number of Overlays
        LinearLayout linearLayoutCategory1, linearLayoutCategory2;
        linearLayoutCategory1 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1);
        linearLayoutCategory2 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2);
        CardView cardViewCategory1 = (CardView) cordLayout.findViewById(R.id.CardViewCategory1);
        CardView cardViewCategory2 = (CardView) cordLayout.findViewById(R.id.CardViewCategory2);


        List<LayerFile> layerFiles = layer.getLayersInPackage();


        for (LayerFile layerFile : layerFiles) {

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            CheckBox check = new CheckBox(this);

            check.setText(layerFile.getNiceName());
            check.setTag(layerFile);

            row.addView(check);

            if (layerFile.isColor()) {
                linearLayoutCategory2.addView(row);
            } else {
                linearLayoutCategory1.addView(row);
            }

            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    refreshFab();
                }
            });

            checkBoxes.add(check);

        }


        //No styleSpecific Overlays
        if (linearLayoutCategory2.getChildCount() == 0) {
            cardViewCategory2.setVisibility(View.GONE);
        }
        //No normal Overlays
        if (linearLayoutCategory1.getChildCount() == 0) {
            cardViewCategory1.setVisibility(View.GONE);
        }


    }

    private void loadScreenshotCardview() {
        loadScreenshots();
    }

    private void receiveAndUseData() {

        TextView tv_description = (TextView) cordLayout.findViewById(R.id.HeX1);
        tv_description.setText(layer.getDescription());

        TextView tv_whatsNew = (TextView) cordLayout.findViewById(R.id.tv_whatsNew);
        tv_whatsNew.setText(layer.getWhatsNew());

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(layer.getName());

    }

    private void createLayouts() {
        //switch to select all Checkboxes
        installEverything = (Switch) cordLayout.findViewById(R.id.allswitch);

        //Hide the FAB
        installationFAB = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        installationFAB.hide();

        //Initialize Layout
        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);


        installationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installTheme();
            }
        });

        installEverything.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkall();
                } else {
                    uncheckAllCheckBoxes();
                }
            }
        });

    }

    private void receiveIntent() {
        String layerPackageName = getIntent().getStringExtra("PackageName");
        try {
            layer = Layer.layerFromPackageName(layerPackageName, getApplicationContext());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        Log.d("PackageName: ", layerPackageName);
    }

    private void loadBackdrop() {

        ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);

        Drawable promo = layer.getPromo();

        imageView.setImageDrawable(promo);

        final CollapsingToolbarLayout Collapsingtoolbar = (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);

        Palette.from(((BitmapDrawable) promo).getBitmap()).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    Collapsingtoolbar.setContentScrimColor(vibrantSwatch.getRgb());
                    float[] hsv = new float[3];
                    Color.colorToHSV(vibrantSwatch.getRgb(), hsv);
                    hsv[2] *= 0.8f;
                    //int colorPrimaryDark = Color.HSVToColor(hsv);
                    Window window = getWindow();
                    window.setStatusBarColor(Color.HSVToColor(hsv));
                }
            }
        });
        Animator reveal = ViewAnimationUtils.createCircularReveal(imageView,
                imageView.getWidth() / 2,
                imageView.getHeight() / 2,
                0,
                imageView.getHeight() * 2);
        reveal.setDuration(750);
        reveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadScreenshotCardview();
                receiveAndUseData();
              //  Animation fadeInAnimation = AnimationUtils.loadAnimation(OverlayDetailActivity.this, R.anim.fadein);
                LinearLayout WN = (LinearLayout) cordLayout.findViewById(R.id.lin2);
                WN.setVisibility(View.VISIBLE);
               // WN.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                    loadOverlayCardviews();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        reveal.start();
    }

    private void loadBackdrop2() {
        final ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);
        imageView.setBackgroundResource(R.drawable.no_heroimage);
    }

    private void loadScreenshots() {
        imageLoader = new LoadDrawables();
        imageLoader.execute();
    }

    //If FAB is clicked
    private void installTheme() {

        boolean isThereColorOverlay = false;

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked() && ((LayerFile) checkBox.getTag()).isColor()) {
                isThereColorOverlay = true;
                break;
            }
        }

        //when a color checkbox is checked
        if (isThereColorOverlay) {
            colorDialog();
        }
        //if only normal Overlays are selected
        else {
            installDialog();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uncheckAllCheckBoxes() {

        for (CheckBox checkBox : checkBoxes) {

            if (checkBox.isChecked()) {
                checkBox.performClick();
            }

        }

    }

    private void checkall() {

        installationFAB.show();

        for (CheckBox checkBox : checkBoxes) {
            if (!checkBox.isChecked()) {
                checkBox.performClick();
            }
        }

    }


    ///////////
    //Snackbars

    private void installationFinishedSnackBar() {

        //show SnackBar after sucessfull installation of the overlays
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content);
        Snackbar.make(coordinatorLayoutView, R.string.OverlaysInstalled, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Commands.reboot(OverlayDetailActivity.this);
                    }
                })
                .show();
    }


    /////////
    //Dialogs
    private void installDialog() {

        //if (showInstallationConfirmDialog()) {
        AlertDialog.Builder installdialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        View dontShowAgainLayout = inflater.inflate(R.layout.dialog_donotshowagain, null);
        dontShowAgain = (CheckBox) dontShowAgainLayout.findViewById(R.id.skip);

        installdialog.setView(dontShowAgainLayout);
        installdialog.setTitle(R.string.MoveOverlays);
        installdialog.setMessage(R.string.ApplyOverlays);
        installdialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()) {
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = myprefs.edit();
                    editor.putString("ConfirmInstallationDialog", "checked");
                    editor.apply();
                }

                //start async task to install the Overlays
                InstallAsyncOverlays();
            }
        });
        installdialog.setNegativeButton(android.R.string.cancel, null);

        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String skipMessage = myprefs.getString("ConfirmInstallationDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            InstallAsyncOverlays();
        }
    }

    private void InstallAsyncOverlays() {

        List<LayerFile> layersToInstall = new ArrayList<>();

        for (CheckBox checkBox : checkBoxes) {

            if (checkBox.isChecked()) {
                LayerFile layerFile = (LayerFile) checkBox.getTag();
                layersToInstall.add(layerFile);
            }

        }

        Log.d("Choosed color", choosedStyle);

        new Commands.InstallOverlaysBetterWay(layersToInstall, choosedStyle, this, this).execute();


    }

    public void processFinish() {
        installationFinishedSnackBar();
        uncheckAllCheckBoxes();
        installEverything.setChecked(false);
    }

    //Dialog to choose color
    private void colorDialog() {

        final AlertDialog.Builder colorDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        colorDialog.setTitle(R.string.pick_color);
        View colordialogView = inflater.inflate(R.layout.dialog_colors, null);
        colorDialog.setView(colordialogView);

        final RadioGroup radioGroup = (RadioGroup) colordialogView.findViewById(R.id.radiogroup);

        RadioGroup.LayoutParams params
                = new RadioGroup.LayoutParams(this, null);

        params.leftMargin = 66;
        params.topMargin = 2;
        params.bottomMargin = 2;
        params.width = RadioGroup.LayoutParams.MATCH_PARENT;

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());
        params.height = height;


        final List<String> colors = layer.getColors();

        for (final String color : colors) {

            final RadioButton radioButton = new RadioButton(this);

            radioButton.setText(color);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(18);
            radioButton.setTag(color);

            radioGroup.addView(radioButton);

            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    choosedStyle = (String) v.getTag();
                }
            });

            if (colors.indexOf(color) == 0) {
                radioButton.performClick();
            }

        }

        colorDialog.setCancelable(false);
        colorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installDialog();
            }
        });
        colorDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }


        });
        colorDialog.create();
        colorDialog.show();
    }

    @Override
    public void onDestroy() {
        loadBackdrop2();
        if (imageLoader.getStatus() != AsyncTask.Status.FINISHED) {
            imageLoader.cancel(true);
        }
        try {
            layer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    private class LoadDrawables extends AsyncTask<Void, Drawable, Void> {

        LinearLayout screenshotLayout;

        @Override
        protected void onPreExecute() {
            screenshotLayout = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutScreenshots);
        }

        @Override
        protected void onProgressUpdate(Drawable... values) {

            for (Drawable screenshot : values) {

                ImageView screenshotImageView;

                try {
                    screenshotImageView = new ImageView(OverlayDetailActivity.this);
                } catch (NullPointerException e) {
                    continue;
                }

                Bitmap bitmap = ((BitmapDrawable) screenshot).getBitmap();

                //TODO: Rewrite
                if (bitmap.getHeight() > 1000) {
                    screenshotImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true));
                } else {
                    screenshotImageView.setImageBitmap(bitmap);
                }

                LinearLayout linear = new LinearLayout(OverlayDetailActivity.this);

                linear.addView(screenshotImageView);
                screenshotLayout.addView(linear);
            }

        }

        @Override
        protected Void doInBackground(Void... params) {

            Pair<Integer, Drawable> pair;

            while ((pair = layer.getNextScreenshot()).first != 0) {

                if (isCancelled()) {
                    break;
                }

                publishProgress(pair.second);

            }

            return null;
        }

    }
}