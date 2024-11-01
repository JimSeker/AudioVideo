package edu.cs4730.cameraxdemo;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;

import edu.cs4730.cameraxdemo.databinding.FragmentDisplayPicBinding;

/**
 * displays the new picture.
 */
public class DisplayPicFragment extends DialogFragment {

    Uri picUri;
    FragmentDisplayPicBinding binding;

    public static DisplayPicFragment newInstance(Uri mediaUri) {
        DisplayPicFragment frag = new DisplayPicFragment();
        Bundle args = new Bundle();
        args.putString("uri", mediaUri.toString());
        frag.setArguments(args);
        return frag;
    }

    void setPic(Bitmap b) {
        binding.imageView.setImageBitmap(b);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        picUri = Uri.parse(requireArguments().getString("uri"));
        Log.wtf("dialog", picUri.toString());

        binding = FragmentDisplayPicBinding.inflate(getLayoutInflater());
        Bitmap bitmap;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                //at some point, I need to do this correctly.  but it still works, so ignoring it.
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().getContentResolver(), picUri));
            } else {
                bitmap = BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(picUri));
            }
            binding.imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(requireActivity(), R.style.AppTheme));
        builder.setView(binding.getRoot()).setTitle("Picture Taken.");
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
        return builder.create();
    }

}