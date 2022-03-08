package edu.cs4730.piccapture2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

/**
 * displays the new picture.
 */
public class DisplayPicFragment extends DialogFragment {

    Uri picUri;

    //take the parameters.
    public static DisplayPicFragment newInstance(Uri mediaUri) {
        DisplayPicFragment frag = new DisplayPicFragment();
        Bundle args = new Bundle();
        args.putString("uri", mediaUri.toString());
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        picUri = Uri.parse(requireArguments().getString("uri"));
        Log.wtf("dialog", picUri.toString());
        ImageView iv;

        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        View myView = inflater.inflate(R.layout.dialog_display_pic, null);
        iv = myView.findViewById(R.id.imageView);
        Bitmap bitmap;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(requireActivity().getContentResolver(), picUri)
                );
            } else {
                bitmap = BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(picUri));
            }
            iv.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }


        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(requireActivity(), R.style.AppTheme_dialog));
        builder.setView(myView).setTitle("Picture Taken.");
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
        return builder.create();
    }

}