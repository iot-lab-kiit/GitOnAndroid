package com.manichord.mgit.ui.dialogs;

import java.util.List;

import com.manichord.mgit.utils.CodeGuesser;
import com.manichord.mgit.ui.fragments.SheimiDialogFragment;
import me.sheimi.sgit.R;
import com.manichord.mgit.ui.ViewFileActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

/**
 * Created by sheimi on 8/16/13.
 */
public class ChooseLanguageDialog extends SheimiDialogFragment {

    private ViewFileActivity mActivity;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = (ViewFileActivity) getActivity();

        final List<String> langs = CodeGuesser.getLanguageList();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setTitle(R.string.dialog_choose_language_title);
        builder.setItems(langs.toArray(new String[0]),
            (dialogInterface, position) -> {
                String lang = langs.get(position);
                String tag = CodeGuesser.getLanguageTag(lang);
                mActivity.setLanguage(tag);
            });

        return builder.create();
    }

}
