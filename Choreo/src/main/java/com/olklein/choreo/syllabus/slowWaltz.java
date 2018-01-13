package com.olklein.choreo.syllabus;

/**
 * Created by olklein on 24/10/2017.
 */

public class slowWaltz {
public static final String[][] FIGURESTEMPOLIST = {
        {"CLOSED CHANGE ON RF",                 "123",                 "Start: RF fwd (Closed Position)\nFinish: RF loses to LF (Closed Position)"},
        {"CLOSED CHANGE ON LF",                 "123",                 "Start: LF fwd (Closed Position)\nFinish: LF closes to RF"},
        {"NATURAL TURN",                        "123 123",             "Start: RF fwd (Closed Position)\nFinish: LF closes to RF"},
        {"REVERSE TURN",                        "123 123",             "Start: LF fwd (Closed Position)\nFinish: RF closes to LF"},
        {"PROGRESSIVE CHASSE TO R",             "12&3",                "Start: LF fwd and slightly to side (Closed Position)\nFinish: RF to side and slightly bwd (Closed Position)"},
        {"WHISK",                               "123",                 "Start: LF fwd (Closed Position)\nFinish: LF crosses behind RF (Promenade Position)"},
        {"BACK WHISK",                          "123",                 "Start: LF bwd in CBMP (Outside Partner Position)\nFinish: LF crosses behind RF (Promenade Position)"},
        {"OUTSIDE CHANGE",                      "123",                 "Start: LF bwd in CBMP (Outside Partner Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"BASIC WEAVE",                         "123 123",             "Start: RF bwd (Closed Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"CHASSE FROM PP",                      "12&3",                "Start: RF fwd and across in CBMP (Promenade Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"BACKWARD LOCK",                       "12&3",                "Start: LF bwd in CBMP (Outside Partner Position)\nFinish: RF diag, bwd (Closed Position)"},
        {"OPEN NATURAL TURN",                   "123",                 "Start: RF fwd and across in CBMP (Promenade Position)\nFinish: RF bwd R Side leading (Closed Position)"},
        {"HESITATION CHANGE",                   "123",                 "Start: LF bwd and slightly to side (Closed Position)\nFinish: LF closes to RF w/o weight-weight on RF (Closed Position)"},
        {"NATURAL SPIN TURN",                   "123 123",             "Start: RF fwd (Closed Position)\nFinish: LF bwd and slightly to side (Closed Position)"},
        {"DOUBLE REVERSE SPIN",                 "123 (12&3 Lady)",     "Start: LF fwd and slightly to side (Closed Position)\nFinish: Weight on RF 123 (12&3 Lady)"},
        {"TELEMARK",                            "123",                 "Start: LF fwd and slightly to side (Closed Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"TELEMARK TO PP",                      "123",                 "Start: LF Fwd and slightly to side (Closed Position)\nFinish: LF to side (Promenade Position)"},
        {"WEAVE FROM PP",                       "123 123",             "Start: RF fwd and across in CBMP (Promenade Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"IMPETUS",                             "123",                 "Start: LF bwd and slightly to side (Closed Position)\nFinish: LF to side and slightly Bwd (Closed Position)"},
        {"IMPETUS TO PP",                       "123",                 "Start: LF Bwd and slightly to side (Closed Position)\nFinish: LF diag Fwd L side leading (Promenade Position)"},
        {"DRAG HESITATION",                     "123",                 "Start: LF Fwd and slightly to side (Closed Position)\nFinish: LF closes to RF w/o weight, weight on RF (Closed Position)"},
        {"OUTSIDE SPIN",                        "123",                 "Start: LF Bwd in CBMP (small step)\nFinish: LF to side"},
        {"NATURAL TURNING LOCK",                "1&23",                "Start: RF bwd with R side leading (Closed Position)\nFinish: LF diag. fwd L side leading (Promenade Position)"},
        {"REVERSE TURNING LOCK",                "1&23",                "Start: RF bwd with R side leading (Closed Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"WING",                                "1 (23)",              "Start: RF fwd in CBMP (Outside Partner Position)\nFinish: LF closes to RF w/o weight, weight on RF (Wing Position)"},
        {"WING FROM PP",                        "1 (23)",              "Start: RF fwd and across in CBMP (Promenade Position)\nFinish: LF closes to RF w/o weight, weight on RF (Wing Position)"},
        {"CROSS HESITATION FROM PP",            "1 (23)",              "Start: RF fwd in CBMP (Promenade Position)\nFinish: Weight on RF"},
        {"REVERSE PIVOT",                       "&",                   "Start: RF diag, bwd outside Lady’s LF (Closed Position)\nFinish: weight on RF (Closed Position)"},
        {"FALLAWAY NATURAL TURN",               "123 123",             "Start: RF fwd and across in CBMP (Promenade Position)\nFinish: LF to side and slightly fwd (Closed Position)"},
        {"RUNNING WEAVE FROM PP",               "1&23 123",            "Start: RF fwd and across in CBMP (Promenade Position)\nFinish: RF crosses behind LF (Closed Position)"},
        {"RUNNING SPIN TURN",                   "123 1&23",            "Start: RF fwd (Closed Position)\nFinish: RF bwd R side leading (Closed Position)"},
        {"OVERTURNED RUNNING SPIN TURN",        "123 1&23 12&3",       "Start: RF fwd (Closed Position)\nFinish: LF closes to RF w/o weight, weight on RF (Wing Position)"},
        {"RUNNING CROSS CHASSE",                "1&23",                "Start: RF fwd in CBMP (Outside Partner position)\nFinish: LF fwd L side leading (Closed Position)"},
        {"FALLAWAY REVERSE AND SLIP PIVOT",     "1&23",                "Start: LF fwd and slightly to side (Closed position)\nFinish: RF bwd (Slip Pivot) weight on RF, LF held in position (Closed Position)"},
        {"HOVER CORTE",                         "123",                 "Start: RF bwd and slightly to side (Closed Position)\nFinish: Transfer weight to RF (Closed Position)"},
        {"CURVED FEATHER",                      "123",                 "Start: RF fwd (Closed Position)\nFinish: RF fwd in CBMP (Outside Partner Position)"},
        {"RUNNING FINISH",                      "123",                 "Start: LF bwd in CBMP (Outside Partner Position)\nFinish: LF fwd L side leading (Closed Position)"},
        {"OUTSIDE SWIVEL",                      "1(23)",               "Start: LF back in CBMP (Outside Partner Position)\nFinish: Weight on LF (Promenade position)"},
        {"PROGRESSIVE CHASSE TO L",             "12&3",                "Start: RF bwd (Closed Position)\nFinish: LF side and slightly fwd (Closed Position)"},
        {"BOUNCE FALLAWAY WEAVE ENDING",        "1&23 123",            "Start: LF fwd and slightly to side (Closed Position)\nFinish: LF side and slightly fwd (Closed Position)"},
        {"QUICK OPEN REVERSE",                  "1&23",                "Start: RF fwd in CBMP (Outside Partner position)\nFinish: LF bwd in CBMP (Outside Partner position)"}


};
}
