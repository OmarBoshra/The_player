package com.omar.acer.musicalstructure;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class dialog {
    private final Context context;
    private Dialog dialogue;

    public dialog(final Context context) {
        this.context = context;
    }

    void Loading() {

      dialogue = new Dialog(context);
      dialogue.setContentView(R.layout.dialogue);

      dialogue.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        final LinearLayout l = dialogue.findViewById(R.id.checkboxes);
        l.setVisibility(View.GONE);
        final Button b = dialogue.findViewById(R.id.ok);
        b.setVisibility(View.GONE);

        TextView tv = dialogue.findViewById(R.id.textView);
        tv.setText("Just a sec..");
        tv.setTextColor(Color.MAGENTA);
        final ConstraintLayout c = dialogue.findViewById(R.id.dialogueback);
        c.setBackgroundColor(context.getResources().getColor(R.color.lighterblack));


        dialogue.getWindow().getAttributes().windowAnimations = R.style.Widget_AppCompat_PopupMenu;

        dialogue.show();


    }

    void dismiss() {
       dialogue.dismiss();
    }

}
