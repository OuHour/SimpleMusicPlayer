package com.example.simplemusicplayer.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.simplemusicplayer.R;
import com.example.simplemusicplayer.activity.PlayerActivity;
import com.example.simplemusicplayer.modal.MusicFiles;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        holder.file_album.setText(mFiles.get(position).getAlbum());
        holder.file_artist.setText(mFiles.get(position).getArtist());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if(image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.album_art);
        }
        else {
            Glide.with(mContext).asBitmap().load(R.drawable.icon_music).into(holder.album_art);
        }
//        holder.album_art.setImageResource(R.drawable.icon_music);

//        String path = mFiles.get(position).getPath();
//        Log.i(path, "this is cover path");

//        Drawable background = holder.album_art.getBackground();
//        if(background == null) {
//            String pathId = mFiles.get(position).getPath();
//            MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
//            metaRetriver.setDataSource(pathId);
//            byte[] art = metaRetriver.getEmbeddedPicture();
//            BitmapFactory.Options opt = new BitmapFactory.Options();
//            opt.inSampleSize = 2;
//            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length,opt);
//            BitmapDrawable ob = new BitmapDrawable(mContext.getResources(), songImage);
//            holder.album_art.setBackground(ob);
//        }
//        else {
//            holder.album_art.setImageResource(R.drawable.icon_music);
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.delete:
                                Toast.makeText(mContext, "Delete Clicked!!", Toast.LENGTH_SHORT).show();
                                deleteFile(position, view);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    private void deleteFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId()));
        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view, "File Deleted", Snackbar.LENGTH_LONG).show();
        }
        else {
            // File maybe in SD card
            Snackbar.make(view, "File Can't be Deleted", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView file_name;
        TextView file_album;
        TextView file_artist;
        ImageView album_art;
        ImageView menuMore;

        public MyViewHolder(View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            file_album = itemView.findViewById(R.id.music_album);
            file_artist = itemView.findViewById(R.id.music_artist);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }

    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }

}
