package com.example.artur_thinkpad.cubescanner;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    static boolean flag;

    static{
        if (!(OpenCVLoader.initDebug())){

        }else {
            flag = true;
        }
    }

    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny, mat, hsvMat;

    double katOdchylenia = 85;
    double maxOdlPomiedzyKoncamiLinii = 10;
    double maxOdlPomiedzyPunktami = 120;
    double maxAngleBetweenPoints = PI/7;
    double maxKatPomiedzyPunktemALinia = PI/18;
    double maxKatPomiedzyPunktemALiniaPoziomo = PI/18;
    double wskaznikPoziomu = 350;


    double[] redL = {145,5,0};
    double[] redH = {235,120,45};
    double[] blueL = {0,45,90};
    double[] blueH = {30,100,245};
    double[] orangeL = {230,120,0};
    double[] orangeH = {285,155,30};
    double[] yellowL = {185,200,10};
    double[] yellowH = {260,265,60};
    double[] greenL = {0,140,20};
    double[] greenH = {30,220,70};
    double[] whiteL = {180,185,185};
    double[] whiteH = {250,265,270};

    double[] red = {255,0,0};
    double[] blue = {0,0,255};
    double[] orange = {255,177,0};
    double[] yellow = {255,255,0};
    double[] green = {0,255,0};
    double[] white = {255,255,255};



    boolean s = false;
    boolean exist = false;


    ArrayList<double[]> punktyNaLinii1 = new ArrayList<>();
    ArrayList<double[]> punktyNaLinii2 = new ArrayList<>();
    ArrayList<double[][]> rownolegleLinie = new ArrayList<>();
    ArrayList<double[]> punktySrodkowe = new ArrayList<>();
    ArrayList<double[]> centers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView)findViewById(R.id.aaa);
        if (flag){
            javaCameraView.setCameraIndex(0);
            javaCameraView.setCvCameraViewListener(this);
            javaCameraView.enableView();
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width,height, CvType.CV_64FC3);
        imgGray = new Mat(width,height, CvType.CV_8UC1);
        imgCanny = new Mat(width,height, CvType.CV_8UC1);
        mat = new Mat(width,height, CvType.CV_8UC1);
        hsvMat = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        imgGray.release();
        imgCanny.release();
        mat.release();
        hsvMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return aaaa(inputFrame);


    }

    private Mat aaaa(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //noc dom

        //mRgba = inputFrame.rgba();
        //Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.equalizeHist(imgGray,mat);
        //Imgproc.blur(mat, mat, new Size(3,3));
        //Imgproc.Canny(mat, imgCanny,100,300,3, false);
        //Imgproc.dilate(imgCanny, imgCanny, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6,6)));


        if(exist){
            while(!s){
                if (s){break;}
            }
            s = false;
        }


        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.equalizeHist(imgGray,mat);
        Imgproc.blur(mat, mat, new Size(3,3));
        Imgproc.Canny(mat, imgCanny,100,300,3, false);
        Imgproc.dilate(imgCanny, imgCanny, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6,6)));

        // Probabilistic Line Transform
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(imgCanny, linesP, 1, Math.PI/180, 150, 0, 0); // runs the actual detection

        double[][] linie = new double[linesP.rows()][];
        //Point[] points = new Point[50];
        ArrayList<double[]> punktyPrzecieciaLinii = new ArrayList<>();

        for (int i = 0; i < linie.length;i++) {
                linie[i] = linesP.get(i, 0);
        }

        int i = 0,j;
        //szukanie linii przeciecia nierownoleglych linii
        while (i < linesP.rows()) {
            double ai = atan2(linie[i][1] - linie[i][3], linie[i][0] - linie[i][2]); // kat nachylenia itej linii
            j = 0;
            while (j < linesP.rows()) {
                double aj = atan2(linie[j][1] - linie[j][3], linie[j][0] - linie[j][2]); // kat nachylenia jtej linii
                if ((i != j) &&
                        (abs(ai - aj) > katOdchylenia * Math.PI / 180) &&
                        (abs(abs(ai - aj) - Math.PI) > katOdchylenia * Math.PI / 180) &&
                        (abs(abs(ai - aj)- PI - PI ) > katOdchylenia * PI /180)) {

                    if(abs( linie[i][0] - linie[j][0]) < maxOdlPomiedzyKoncamiLinii &&
                            abs(linie[i][1] - linie[j][1]) < maxOdlPomiedzyKoncamiLinii)
                    {
                        double[] p = new double[4];
                        p[0]= linie[i][0];
                        p[1]= linie[i][1];

                        if(ai > aj) {
                            p[2]= aj;
                            p[3]= ai;
                        } else {
                            p[2]= ai;
                            p[3]= aj;
                        }

                        if(p[2] > Math.PI ) {
                            p[2] -= Math.PI ;
                        }
                        if(p[2] < 0) {
                            p[2] += Math.PI ;
                        }
                        if(p[3] > Math.PI ) {
                            p[3] -= Math.PI ;
                        }
                        if(p[3] < 0) {
                            p[3] += Math.PI ;
                        }

                        punktyPrzecieciaLinii.add(p);
                    }
                }
                j++;
            }
            i++;
        }

        i=0;


        while (i < punktyPrzecieciaLinii.size()) {
            j = 0;
            while (j < punktyPrzecieciaLinii.size()) {
                //scalanie wykrytych punktow
                double r = Math.sqrt(( punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0])*( punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) +
                        ( punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1])*( punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1])); // odleglosc miedzy punktami
                if(i!= j && r < maxOdlPomiedzyPunktami /*&&
                        comparePairsOfAngles( points.get(i)[2] , points.get(i)[3] , points.get(j)[2] ,
                        points.get(j)[3] , maxAngleBetweenPoints )*/) {

                    punktyPrzecieciaLinii.get(i)[0] = ( punktyPrzecieciaLinii.get(i)[0] + punktyPrzecieciaLinii.get(j)[0])/2;
                    punktyPrzecieciaLinii.get(i)[1] = ( punktyPrzecieciaLinii.get(i)[1] + punktyPrzecieciaLinii.get(j)[1])/2;
                    punktyPrzecieciaLinii.get(i)[2] = ( punktyPrzecieciaLinii.get(i)[2] + punktyPrzecieciaLinii.get(j)[2])/2;
                    punktyPrzecieciaLinii.get(i)[3] = ( punktyPrzecieciaLinii.get(i)[3] + punktyPrzecieciaLinii.get(j)[3])/2;

                    punktyPrzecieciaLinii.remove(j);
                    if(j < i) {
                        i--;
                    }
                }else j++;
            }
            i++;
        }

        i=0;

        while (i < punktyPrzecieciaLinii.size()) {
            j = 0;
            while (j < punktyPrzecieciaLinii.size()) {
                double ai2j = atan2(punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1], punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]);
                //pionowe
                if (i != j &&
                        comparePairsOfAngles(punktyPrzecieciaLinii.get(i)[2], punktyPrzecieciaLinii.get(i)[3], punktyPrzecieciaLinii.get(j)[2], punktyPrzecieciaLinii.get(j)[3], maxAngleBetweenPoints) &&
                        ((abs(ai2j - punktyPrzecieciaLinii.get(i)[2]) < maxKatPomiedzyPunktemALinia) ||
                        (abs(abs(ai2j - punktyPrzecieciaLinii.get(j)[2]) - PI) < maxKatPomiedzyPunktemALinia))) {
                    double rij = Math.sqrt(( punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0])*( punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) +
                            ( punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1])*( punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1]));
                    int iloscPunktowWOdcinku = 0;
                    int nrPunktu13 = -1, nrPunktu23 = -1;
                    double[][] punktyNaLinii11 = new double[4][];

                    if (abs(punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) > wskaznikPoziomu) { //linia jest pozioma
                        if (punktyPrzecieciaLinii.get(i)[0] < punktyPrzecieciaLinii.get(j)[0] ){
                            punktyNaLinii1.add(punktyPrzecieciaLinii.get(i));
                        }else{
                            punktyNaLinii1.add(punktyPrzecieciaLinii.get(j));
                        }
                    }else{ //linia jest pionowa
                        if (punktyPrzecieciaLinii.get(i)[1] < punktyPrzecieciaLinii.get(j)[1] ){
                            punktyNaLinii1.add(punktyPrzecieciaLinii.get(i));
                        }else{
                            punktyNaLinii1.add(punktyPrzecieciaLinii.get(j));
                        }
                    }

                    for (int l = 0; l < punktyPrzecieciaLinii.size(); l++) {

                        double ai2l = atan2(punktyPrzecieciaLinii.get(l)[1] - punktyPrzecieciaLinii.get(i)[1], punktyPrzecieciaLinii.get(l)[0] - punktyPrzecieciaLinii.get(i)[0]);

                        if (i != l &&
                                l != j &&
                                ((abs(ai2l - punktyPrzecieciaLinii.get(i)[2]) < maxKatPomiedzyPunktemALinia) ||
                                (abs(abs(ai2l - punktyPrzecieciaLinii.get(i)[2]) - PI) < maxKatPomiedzyPunktemALinia))) {
                            double ril = Math.sqrt((punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(l)[0]) * (punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(l)[0]) +
                                    (punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(l)[1]) * (punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(l)[1]));
                            double rjl = Math.sqrt((punktyPrzecieciaLinii.get(j)[0] - punktyPrzecieciaLinii.get(l)[0]) * (punktyPrzecieciaLinii.get(j)[0] - punktyPrzecieciaLinii.get(l)[0]) +
                                    (punktyPrzecieciaLinii.get(j)[1] - punktyPrzecieciaLinii.get(l)[1]) * (punktyPrzecieciaLinii.get(j)[1] - punktyPrzecieciaLinii.get(l)[1]));
                            // punkt przecina odcinek w stosunku 1:2
                            if (ril < rij && rjl < rij && abs(ril - rij/3) < 80) {
                                nrPunktu13 = l;
                            }
                            // punkt przecina odcinek w stosunku 2:1
                            if (ril < rij && rjl < rij && abs(ril - rij*(2/3)) < 80) {
                                nrPunktu23 = l;
                            }
                            // ogolnie lezy na odcinku
                            if (ril < rij && rjl < rij) {
                                if ((nrPunktu13 == -1 || nrPunktu23 == -1) && iloscPunktowWOdcinku < 2){
                                    punktySrodkowe.add(punktyPrzecieciaLinii.get(l));
                                    iloscPunktowWOdcinku++;
                                }
                            }
                        }
                    }

                    if(iloscPunktowWOdcinku == 2){
                        if (abs(punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) > wskaznikPoziomu) { //linia jest pozioma
                            if (punktySrodkowe.get(0)[0] < punktySrodkowe.get(1)[0] ){
                                punktyNaLinii1.add(punktySrodkowe.get(0));
                                punktyNaLinii1.add(punktySrodkowe.get(1));
                            }else{
                                punktyNaLinii1.add(punktySrodkowe.get(1));
                                punktyNaLinii1.add(punktySrodkowe.get(0));
                            }
                        }else{ //linia jest pionowa
                            if (punktySrodkowe.get(0)[1] < punktySrodkowe.get(1)[1] ){
                                punktyNaLinii1.add(punktySrodkowe.get(0));
                                punktyNaLinii1.add(punktySrodkowe.get(1));
                            }else{
                                punktyNaLinii1.add(punktySrodkowe.get(1));
                                punktyNaLinii1.add(punktySrodkowe.get(0));
                            }
                        }

                        if (abs(punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) > wskaznikPoziomu) { //linia jest pozioma
                            if (punktyPrzecieciaLinii.get(i)[0] < punktyPrzecieciaLinii.get(j)[0] ){
                                punktyNaLinii1.add(punktyPrzecieciaLinii.get(j));
                            }else{
                                punktyNaLinii1.add(punktyPrzecieciaLinii.get(i));
                            }
                        }else{ //linia jest pionowa
                            if (punktyPrzecieciaLinii.get(i)[1] < punktyPrzecieciaLinii.get(j)[1] ){
                                punktyNaLinii1.add(punktyPrzecieciaLinii.get(j));
                            }else{
                                punktyNaLinii1.add(punktyPrzecieciaLinii.get(i));
                            }
                        }
                        punktyNaLinii1.toArray(punktyNaLinii11);
                        rownolegleLinie.add(punktyNaLinii11);
                    }
                    punktyNaLinii1.clear();
                    punktySrodkowe.clear();
                    //poziome
                }
                if (i != j &&
                        /*comparePairsOfAngles(points.get(i)[2], points.get(i)[3], points.get(j)[2], points.get(j)[3], maxAngleBetweenPoints) &&*/
                        ((abs(ai2j - punktyPrzecieciaLinii.get(i)[3]) < maxKatPomiedzyPunktemALiniaPoziomo) ||
                                (abs(abs(ai2j - punktyPrzecieciaLinii.get(j)[3]) - PI) < maxKatPomiedzyPunktemALiniaPoziomo))) {
                    double rij = Math.sqrt(( punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0])*( punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) +
                            ( punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1])*( punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(j)[1]));
                    int iloscPunktowWOdcinkuPoziomym = 0;
                    int nrPunktu13 = -1, nrPunktu23 = -1;
                    double[][] punktyNaLinii22 = new double[4][];

                    if (abs(punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) > wskaznikPoziomu) { //linia jest pozioma
                        if (punktyPrzecieciaLinii.get(i)[0] < punktyPrzecieciaLinii.get(j)[0] ){
                            punktyNaLinii2.add(punktyPrzecieciaLinii.get(i));
                        }else{
                            punktyNaLinii2.add(punktyPrzecieciaLinii.get(j));
                        }
                    }else{ //linia jest pionowa
                        if (punktyPrzecieciaLinii.get(i)[1] < punktyPrzecieciaLinii.get(j)[1] ){
                            punktyNaLinii2.add(punktyPrzecieciaLinii.get(i));
                        }else{
                            punktyNaLinii2.add(punktyPrzecieciaLinii.get(j));
                        }
                    }
                    for (int l = 0; l < punktyPrzecieciaLinii.size(); l++) {

                        double ai2l = atan2((double) punktyPrzecieciaLinii.get(l)[1] - punktyPrzecieciaLinii.get(i)[1], (double) punktyPrzecieciaLinii.get(l)[0] - punktyPrzecieciaLinii.get(i)[0]);

                        if (i != l &&
                                l != j &&
                                ((abs(ai2l - punktyPrzecieciaLinii.get(i)[3]) < maxKatPomiedzyPunktemALiniaPoziomo) ||
                                        (abs(abs(ai2l - punktyPrzecieciaLinii.get(i)[3]) - PI) < maxKatPomiedzyPunktemALiniaPoziomo))) {
                            double ril = Math.sqrt((double) (punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(l)[0]) * (punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(l)[0]) +
                                    (punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(l)[1]) * (punktyPrzecieciaLinii.get(i)[1] - punktyPrzecieciaLinii.get(l)[1]));
                            double rjl = Math.sqrt((double) (punktyPrzecieciaLinii.get(j)[0] - punktyPrzecieciaLinii.get(l)[0]) * (punktyPrzecieciaLinii.get(j)[0] - punktyPrzecieciaLinii.get(l)[0]) +
                                    (punktyPrzecieciaLinii.get(j)[1] - punktyPrzecieciaLinii.get(l)[1]) * (punktyPrzecieciaLinii.get(j)[1] - punktyPrzecieciaLinii.get(l)[1]));
                            // punkt przecina odcinek w stosunku 1:2
                            if (ril < rij && rjl < rij && abs(ril - rij/3) < 80) {
                                nrPunktu13 = l;
                            }
                            // punkt przecina odcinek w stosunku 2:1
                            if (ril < rij && rjl < rij && abs(ril - rij*(2/3)) < 80) {
                                nrPunktu23 = l;
                            }
                            // ogolnie lezy na odcinku
                            if (ril < rij && rjl < rij) {
                                if ((nrPunktu13 == -1 || nrPunktu23 == -1) && iloscPunktowWOdcinkuPoziomym < 2){
                                    punktySrodkowe.add(punktyPrzecieciaLinii.get(l));
                                    iloscPunktowWOdcinkuPoziomym++;
                                }
                            }
                        }
                    }
                    if(iloscPunktowWOdcinkuPoziomym == 2 /*&& nrPunktu13 == 1 && nrPunktu23 ==  1*/){
                        if (abs(punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) > wskaznikPoziomu) { //linia jest pozioma
                            if (punktySrodkowe.get(0)[0] < punktySrodkowe.get(1)[0] ){
                                punktyNaLinii2.add(punktySrodkowe.get(0));
                                punktyNaLinii2.add(punktySrodkowe.get(1));
                            }else{
                                punktyNaLinii2.add(punktySrodkowe.get(1));
                                punktyNaLinii2.add(punktySrodkowe.get(0));
                            }
                        }else{ //linia jest pionowa
                            if (punktySrodkowe.get(0)[1] < punktySrodkowe.get(1)[1] ){
                                punktyNaLinii2.add(punktySrodkowe.get(0));
                                punktyNaLinii2.add(punktySrodkowe.get(1));
                            }else{
                                punktyNaLinii2.add(punktySrodkowe.get(1));
                                punktyNaLinii2.add(punktySrodkowe.get(0));
                            }
                        }

                        if (abs(punktyPrzecieciaLinii.get(i)[0] - punktyPrzecieciaLinii.get(j)[0]) > wskaznikPoziomu) { //linia jest pozioma
                            if (punktyPrzecieciaLinii.get(i)[0] < punktyPrzecieciaLinii.get(j)[0] ){
                                punktyNaLinii2.add(punktyPrzecieciaLinii.get(j));
                            }else{
                                punktyNaLinii2.add(punktyPrzecieciaLinii.get(i));
                            }
                        }else{ //linia jest pionowa
                            if (punktyPrzecieciaLinii.get(i)[1] < punktyPrzecieciaLinii.get(j)[1] ){
                                punktyNaLinii2.add(punktyPrzecieciaLinii.get(j));
                            }else{
                                punktyNaLinii2.add(punktyPrzecieciaLinii.get(i));
                            }
                        }
                        punktyNaLinii2.toArray(punktyNaLinii22);
                        rownolegleLinie.add(punktyNaLinii22);
                    }
                    punktyNaLinii2.clear();
                    punktySrodkowe.clear();
                }
                j++;
            }
            i++;
        }

        double[][] linia1 = new double[4][4];
        double[][] linia2 = new double[4][4];
        double[] wspolnyPunkt = new double[4];
        boolean mamLinie = false;

        for (int n = 0; n < rownolegleLinie.size();n++){
            double liczbaProstopadlychLinii = 0;
            double liczbaWspolnychPunktow = 0;
            for (int x = 0; x < rownolegleLinie.size();x++){
                if (n != x && liczbaProstopadlychLinii != 2) {
                    for (int m = 0;m < 4;m++){
                        for (int k = 0;k < 4;k++){
                            if (rownolegleLinie.get(n)[m][0] == rownolegleLinie.get(x)[k][0] && rownolegleLinie.get(n)[m][1] == rownolegleLinie.get(x)[k][1]) {
                                liczbaWspolnychPunktow++;
                                for (int u = 0;u < 4;u++){
                                    wspolnyPunkt[u] = rownolegleLinie.get(x)[k][u];
                                }
                            }
                        }
                    }
                    if (liczbaWspolnychPunktow == 1){
                        liczbaProstopadlychLinii = 2;
                        mamLinie = true;
                        for (int g = 0;g < 4;g++) {
                            for (int h = 0; h < 4; h++) {
                                linia1[g][h] = rownolegleLinie.get(n)[g][h];
                                linia2[g][h] = rownolegleLinie.get(x)[g][h];
                            }
                        }
                    }else {mamLinie = false;}
                }
            }
        }

        double[][] wektorPrzesuniec = new double[4][2];

        for (int n = 0;n < 4;n++){
            wektorPrzesuniec[n][0] = wspolnyPunkt[0] - linia2[n][0];
            wektorPrzesuniec[n][1] = wspolnyPunkt[1] - linia2[n][1];
        }

        boolean x = false;

        if (mamLinie){
            punktyPrzecieciaLinii.clear();
            if (abs(linia1[0][0] - linia1[3][0]) >  wskaznikPoziomu) {// linia1 jest pozioma
                x = true;
                for (int g = 0;g < 4;g++) {
                    for (int h = 0; h < 4; h++) {
                        punktyPrzecieciaLinii.add(new double[]{linia1[h][0] - wektorPrzesuniec[g][0], linia1[h][1] - wektorPrzesuniec[g][1]});
                    }
                }
            }else {
                x = true;
                for (int g = 0; g < 4; g++) {
                    for (int h = 0; h < 4; h++) {
                        punktyPrzecieciaLinii.add(new double[]{linia1[g][0] - wektorPrzesuniec[h][0], linia1[g][1] - wektorPrzesuniec[h][1]});
                    }
                }
            }
        }

        double[][] srodki = new double[9][2];

        if (mamLinie && x){
            double[] srodek = new double[2];
            //  1
            srodek[0] = (punktyPrzecieciaLinii.get(0)[0]+punktyPrzecieciaLinii.get(1)[0]+punktyPrzecieciaLinii.get(4)[0]+punktyPrzecieciaLinii.get(5)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(0)[1]+punktyPrzecieciaLinii.get(1)[1]+punktyPrzecieciaLinii.get(4)[1]+punktyPrzecieciaLinii.get(5)[1])/4;
            srodki[0][0]=srodek[0];
            srodki[0][1]=srodek[1];
            //  2
            srodek[0] = (punktyPrzecieciaLinii.get(1)[0]+punktyPrzecieciaLinii.get(2)[0]+punktyPrzecieciaLinii.get(5)[0]+punktyPrzecieciaLinii.get(6)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(1)[1]+punktyPrzecieciaLinii.get(2)[1]+punktyPrzecieciaLinii.get(5)[1]+punktyPrzecieciaLinii.get(6)[1])/4;
            srodki[1][0]=srodek[0];
            srodki[1][1]=srodek[1];
            //  3
            srodek[0] = (punktyPrzecieciaLinii.get(2)[0]+punktyPrzecieciaLinii.get(3)[0]+punktyPrzecieciaLinii.get(6)[0]+punktyPrzecieciaLinii.get(7)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(2)[1]+punktyPrzecieciaLinii.get(3)[1]+punktyPrzecieciaLinii.get(6)[1]+punktyPrzecieciaLinii.get(7)[1])/4;
            srodki[2][0]=srodek[0];
            srodki[2][1]=srodek[1];
            //  4
            srodek[0] = (punktyPrzecieciaLinii.get(4)[0]+punktyPrzecieciaLinii.get(5)[0]+punktyPrzecieciaLinii.get(8)[0]+punktyPrzecieciaLinii.get(9)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(4)[1]+punktyPrzecieciaLinii.get(5)[1]+punktyPrzecieciaLinii.get(8)[1]+punktyPrzecieciaLinii.get(9)[1])/4;
            srodki[3][0]=srodek[0];
            srodki[3][1]=srodek[1];
            //  5
            srodek[0] = (punktyPrzecieciaLinii.get(5)[0]+punktyPrzecieciaLinii.get(6)[0]+punktyPrzecieciaLinii.get(9)[0]+punktyPrzecieciaLinii.get(10)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(5)[1]+punktyPrzecieciaLinii.get(6)[1]+punktyPrzecieciaLinii.get(9)[1]+punktyPrzecieciaLinii.get(10)[1])/4;
            srodki[4][0]=srodek[0];
            srodki[4][1]=srodek[1];
            //  6
            srodek[0] = (punktyPrzecieciaLinii.get(6)[0]+punktyPrzecieciaLinii.get(7)[0]+punktyPrzecieciaLinii.get(10)[0]+punktyPrzecieciaLinii.get(11)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(6)[1]+punktyPrzecieciaLinii.get(7)[1]+punktyPrzecieciaLinii.get(10)[1]+punktyPrzecieciaLinii.get(11)[1])/4;
            srodki[5][0]=srodek[0];
            srodki[5][1]=srodek[1];
            //  7
            srodek[0] = (punktyPrzecieciaLinii.get(8)[0]+punktyPrzecieciaLinii.get(9)[0]+punktyPrzecieciaLinii.get(12)[0]+punktyPrzecieciaLinii.get(13)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(8)[1]+punktyPrzecieciaLinii.get(9)[1]+punktyPrzecieciaLinii.get(12)[1]+punktyPrzecieciaLinii.get(13)[1])/4;
            srodki[6][0]=srodek[0];
            srodki[6][1]=srodek[1];
            //  8
            srodek[0] = (punktyPrzecieciaLinii.get(9)[0]+punktyPrzecieciaLinii.get(10)[0]+punktyPrzecieciaLinii.get(13)[0]+punktyPrzecieciaLinii.get(14)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(9)[1]+punktyPrzecieciaLinii.get(10)[1]+punktyPrzecieciaLinii.get(13)[1]+punktyPrzecieciaLinii.get(14)[1])/4;
            srodki[7][0]=srodek[0];
            srodki[7][1]=srodek[1];
            //  9
            srodek[0] = (punktyPrzecieciaLinii.get(10)[0]+punktyPrzecieciaLinii.get(11)[0]+punktyPrzecieciaLinii.get(14)[0]+punktyPrzecieciaLinii.get(15)[0])/4;
            srodek[1] = (punktyPrzecieciaLinii.get(10)[1]+punktyPrzecieciaLinii.get(11)[1]+punktyPrzecieciaLinii.get(14)[1]+punktyPrzecieciaLinii.get(15)[1])/4;
            srodki[8][0]=srodek[0];
            srodki[8][1]=srodek[1];

            for(int m = 0;m < 9;m++){
                Imgproc.circle(mRgba, new Point(srodki[m][0], srodki[m][1]), 20, new Scalar(255, 255, 255), 8);
            }

        }


        //Imgproc.circle(mRgba, new Point(100, 100), 20, new Scalar(255, 0, 0), 1);
        //Imgproc.circle(mRgba, new Point(400, 100), 20, new Scalar(0, 255, 255), 1);

        boolean punktSpozaObrazu = false;

        if(mamLinie){
            int x0 = (int)mRgba.width()/2;
            int y0 = (int)mRgba.height()/2;

            for (int n = 0;n < srodki.length;n++){
                if((abs(srodki[n][0] - x0) > (x0-20)) || (abs(srodki[n][1] - y0) > (y0-20))){
                    punktSpozaObrazu = true;
                }
            }
        }

        if (mamLinie && !punktSpozaObrazu){
            double bgr[][] = new double[9][3];
            double BGR[] = {0,0,0};
            int posX = 100, posY = 100;
            int posXX = 1000, posYY = 100;


            for (int n = 0;n < srodki.length;n++){
                if (n == 3 || n == 6 || n ==9){
                    posX = 100;
                    posY += 100;
                }

                double xx = srodki[n][0];
                double yy = srodki[n][1];
                Point p = new Point(xx,yy);

                bgr[0] = mRgba.get((int)yy, (int)xx);
                bgr[1] = mRgba.get((int)yy-4, (int)xx);
                bgr[2] = mRgba.get((int)yy-4, (int)xx+4);
                bgr[3] = mRgba.get((int)yy-4, (int)xx-4);
                bgr[4] = mRgba.get((int)yy+4, (int)xx+4);
                bgr[5] = mRgba.get((int)yy+4, (int)xx-4);
                bgr[6] = mRgba.get((int)yy+4, (int)xx);
                bgr[7] = mRgba.get((int)yy, (int)xx-4);
                bgr[8] = mRgba.get((int)yy, (int)xx+4);

                for(int m = 0;m < bgr.length;m++){
                    BGR[0] += bgr[m][0];
                    BGR[1] += bgr[m][1];
                    BGR[2] += bgr[m][2];
                }

                BGR[0] = BGR[0]/bgr.length;
                BGR[1] = BGR[1]/bgr.length;
                BGR[2] = BGR[2]/bgr.length;

                //ustawianie kolorow
                Imgproc.putText(mRgba, new Integer((int)BGR[0]).toString(), new Point(posXX,posYY),1,3,new Scalar(0,255,0),3);
                Imgproc.putText(mRgba, new Integer((int)BGR[1]).toString(), new Point(posXX+200,posYY),1,3,new Scalar(0,255,0),3);
                Imgproc.putText(mRgba, new Integer((int)BGR[2]).toString(), new Point(posXX+400,posYY),1,3,new Scalar(0,255,0),3);

                if (redL[2] <= BGR[2] && BGR[2] <= redH[2] && redL[1] <= BGR[1] && BGR[1] <= redH[1] && redL[0] <= BGR[0] && BGR[0] <= redH[0]) {
                    Imgproc.circle(mRgba, new Point(posX, posY), 30, new Scalar(red[0],red[1], red[2]), 30);
                }else if (greenL[2] <= BGR[2] && BGR[2] <= greenH[2] && greenL[1] <= BGR[1] && BGR[1] <= greenH[1] && greenL[0] <= BGR[0] && BGR[0] <= greenH[0]) {
                    Imgproc.circle(mRgba, new Point(posX, posY), 30, new Scalar(green[0],green[1], green[2]), 30);
                }else if (yellowL[2] <= BGR[2] && BGR[2] <= yellowH[2] && yellowL[1] <= BGR[1] && BGR[1] <= yellowH[1] && yellowL[0] <= BGR[0] && BGR[0] <= yellowH[0]) {
                    Imgproc.circle(mRgba, new Point(posX, posY), 30, new Scalar(yellow[0], yellow[1], yellow[2]), 30);
                }else if (whiteL[2] <= BGR[2] && BGR[2] <= whiteH[2] && whiteL[1] <= BGR[1] && BGR[1] <= whiteH[1] && whiteL[0] <= BGR[0] && BGR[0] <= whiteH[0]) {
                    Imgproc.circle(mRgba, new Point(posX, posY), 30, new Scalar(white[0], white[1], white[2]), 30);
                }else if ((blueL[2] <= BGR[2]) && (BGR[2] <= blueH[2]) && (blueL[1] <= BGR[1]) && (BGR[1] <= blueH[1]) && (blueL[0] <= BGR[0]) && (BGR[0] <= blueH[0])) {
                    Imgproc.circle(mRgba, new Point(posX, posY), 30, new Scalar(blue[0], blue[1], blue[2]), 30);
                }else{
                    Imgproc.circle(mRgba, new Point(posX, posY), 30, new Scalar(orange[0], orange[1], orange[2]), 30);
                }
                posYY += 100;
                posX += 100;
            }
            exist = true;
        }


        punktyPrzecieciaLinii.clear();
        rownolegleLinie.clear();
        centers.clear();
        punktyNaLinii1.clear();
        punktyNaLinii2.clear();
        return mRgba;
    }


    public void send(View view) {
        s = true;
    }



    public static byte[][] getMultiChannelArray(Mat m) {
        //first index is pixel, second index is channel
        int numChannels=m.channels();//is 3 for 8UC3 (e.g. RGB)
        int frameSize=m.rows()*m.cols();
        byte[] byteBuffer= new byte[frameSize*numChannels];
        m.get(0,0,byteBuffer);

        //write to separate R,G,B arrays
        byte[][] out=new byte[frameSize][numChannels];
        for (int p=0,i = 0; p < frameSize; p++) {
            for (int n = 0; n < numChannels; n++,i++) {
                out[p][n]=byteBuffer[i];
            }
        }
        return out;
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (javaCameraView.isEnabled()){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        javaCameraView.enableView();
    }


    public boolean comparePairsOfAngles(double a1 ,double a2 ,double b1 ,double b2 , double ga ){
        boolean abc;

        if((abs(a1 - b1) <= ga || abs(a1 - b2) <= ga) && (abs(a2 - b1) <= ga || abs(a2 - b2) <= ga)){
            abc = true;
        }else {
            abc = false;
        }

        return abc;
    }


}