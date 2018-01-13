package com.olklein.choreo;

import android.content.Context;

import com.olklein.choreo.syllabus.VienneseWaltz;
import com.olklein.choreo.syllabus.slowFox;
import com.olklein.choreo.syllabus.slowWaltz;
import com.olklein.choreo.syllabus.tango;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.olklein.choreo.R.string.slowfox;

/**
 * Created by olklein on 15/07/2016.
 */
class Syllabus {

    public static ArrayList<String> figures = new ArrayList<>();
    private static final ArrayList<String> figuresRhythm = new ArrayList<>();
    public static ArrayList<String[]> figuresWithTempo;
    private static int dance=R.id.allDances;
    private static String danceName="";
    private static String danceShortName="";

    public static void init(Context context){
        setDance(context,dance);
    }


    public static ArrayList<String> getRhythmFor( String figureName){

            figuresRhythm.clear();
            int i =0;
            while (i<figuresWithTempo.size()){
                if (figuresWithTempo.get(i)[0].equals(figureName)){
                    figuresRhythm.add(figuresWithTempo.get(i)[1]);
                }
                i++;
            }

        Set<String> figuresWithoutDuplicates = new LinkedHashSet<>(figuresRhythm);
        // clear the ArrayList
        figuresRhythm.clear();

        // copy elements without any duplicates
        figuresRhythm.addAll(figuresWithoutDuplicates);

        return figuresRhythm;
    }

    public static void setDance(Context context,int dance) {
        setDanceId(dance);
        Syllabus.figures= new ArrayList<>();
        Syllabus.figuresWithTempo= new ArrayList<>();
        int i;

        switch (dance) {
        case R.id.allDances:
            setDanceName(context.getResources().getString(R.string.allDances));
            setDanceShortName(context.getResources().getString(R.string.allDancesShort));
                i=0;
                while (i< slowWaltz.FIGURESTEMPOLIST.length){
                    figures.add(slowWaltz.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(slowWaltz.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< tango.FIGURESTEMPOLIST.length){
                    figures.add(tango.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(tango.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< VienneseWaltz.FIGURESTEMPOLIST.length){
                    figures.add(VienneseWaltz.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(VienneseWaltz.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< slowFox.FIGURESTEMPOLIST.length){
                    figures.add(slowFox.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(slowFox.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i]);
                    i++;
                }
            break;
        case R.id.slowWaltz:
            setDanceName(context.getResources().getString(R.string.slowWaltz));
            setDanceShortName(context.getResources().getString(R.string.SW));

            i=0;
            while (i< slowWaltz.FIGURESTEMPOLIST.length){
                figures.add(slowWaltz.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.slowWaltz.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.tango:
            setDanceName(context.getResources().getString(R.string.tango));
            setDanceShortName(context.getResources().getString(R.string.TG));
            i=0;
            while (i< tango.FIGURESTEMPOLIST.length){
                figures.add(tango.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.tango.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.vienneseWaltz:
            setDanceName(context.getResources().getString(R.string.vienneseWaltz));
            setDanceShortName(context.getResources().getString(R.string.VW));
            i=0;
            while (i< VienneseWaltz.FIGURESTEMPOLIST.length){
                figures.add(VienneseWaltz.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.VienneseWaltz.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.Slowfox:
            setDanceName(context.getResources().getString(slowfox));
            setDanceShortName(context.getResources().getString(R.string.SF));
            i=0;
            while (i< slowFox.FIGURESTEMPOLIST.length){
                figures.add(slowFox.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.slowFox.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.quickstep:
            setDanceName(context.getResources().getString(R.string.quickstep));
            setDanceShortName(context.getResources().getString(R.string.QS));
            i=0;
            while (i< com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.samba:
            setDanceName(context.getResources().getString(R.string.samba));
            setDanceShortName(context.getResources().getString(R.string.SB));
            i=0;
            while (i< com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.rumba:
            setDanceName(context.getResources().getString(R.string.rumba));
            setDanceShortName(context.getResources().getString(R.string.RB));
            i=0;
            while (i< com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.chacha:
            setDanceName(context.getResources().getString(R.string.chacha));
            setDanceShortName(context.getResources().getString(R.string.CC));
            i=0;
            while (i< com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.paso:
            setDanceName(context.getResources().getString(R.string.paso));
            setDanceShortName(context.getResources().getString(R.string.PD));
            i=0;
            while (i< com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.jive:
            setDanceName(context.getResources().getString(R.string.jive));
            setDanceShortName(context.getResources().getString(R.string.JV));
            i=0;
            while (i< com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        }
        Collections.sort(figures, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });


        Comparator<String[]> comparator = new Comparator<String[]>() {
            @Override
            public int compare(String s1[], String s2[]) {
                return s1[0].compareTo(s2[0]);
            }
        };
        Collections.sort(figuresWithTempo,comparator);


        Set<String> figuresWithoutDuplicates = new LinkedHashSet<>(figures);
        // clear the ArrayList
        figures.clear();

        // copy elements but without any duplicates
        figures.addAll(figuresWithoutDuplicates);
    }

    private static void setDanceName(String string) {
        danceName =string;
    }
    private static void setDanceShortName(String string) {
        danceShortName =string;
    }

    public static String getName() {
        return danceName;
    }
    public static String getDanceShortName() {
        return danceShortName;
    }
    public static int getDanceId(){
        return dance;
    }
    private static void setDanceId(int id){
        dance = id;
    }
}