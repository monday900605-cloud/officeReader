package com.wxiwei.office.officereader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.wxiwei.office.constant.MainConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple activity that scans for doc/xls/ppt files and shows them in a list.
 */
public class SimpleDocumentListActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 0x2101;
    private static final int MAX_RESULTS = 200;
    private static final String[] SUPPORTED_EXTENSIONS = new String[]
    {
        ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    };

    private final List<File> documents = new ArrayList<File>();
    private DocumentAdapter adapter;
    private ExecutorService executor;
    private ListView listView;
    private TextView emptyView;
    private ProgressBar progressView;
    private boolean isLoadingDocuments;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_document_list);
        setTitle(R.string.simple_doc_title);

        listView = findViewById(R.id.document_list);
        emptyView = findViewById(R.id.document_empty);
        progressView = findViewById(R.id.document_progress);

        adapter = new DocumentAdapter(documents);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                openDocument(documents.get(position));
            }
        });

        if (hasStoragePermission())
        {
            loadDocuments();
        }
        else
        {
            requestStoragePermission();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (executor != null)
        {
            executor.shutdownNow();
            executor = null;
        }
    }

    private boolean hasStoragePermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            return Environment.isExternalStorageManager();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        for (String permission : getRequiredPermissions())
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    private void requestStoragePermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            try
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
            catch (Exception e)
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(getRequiredPermissions(), REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION)
        {
            if (areAllPermissionsGranted(grantResults))
            {
                loadDocuments();
            }
            else
            {
                Toast.makeText(this, R.string.simple_doc_permission_denied, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private String[] getRequiredPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            return new String[]
            {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            };
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
        }
        else
        {
            return new String[]
            {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    private boolean areAllPermissionsGranted(int[] grantResults)
    {
        if (grantResults == null || grantResults.length == 0)
        {
            return false;
        }
        for (int result : grantResults)
        {
            if (result != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!isLoadingDocuments && documents.isEmpty() && hasStoragePermission())
        {
            loadDocuments();
        }
    }

    private void loadDocuments()
    {
        if (isLoadingDocuments)
        {
            return;
        }
        isLoadingDocuments = true;
        showLoading(true);
        if (executor == null)
        {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                List<File> data = null;
                try
                {
                    data = collectDocuments();
                }
                catch (SecurityException e)
                {
                    data = null;
                }
                final List<File> result = data;
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        isLoadingDocuments = false;
                        if (result == null)
                        {
                            handlePermissionDenied();
                            showLoading(false);
                        }
                        else
                        {
                            displayDocuments(result);
                        }
                    }
                });
            }
        });
    }

    private void displayDocuments(List<File> data)
    {
        documents.clear();
        if (data != null)
        {
            documents.addAll(data);
        }
        adapter.notifyDataSetChanged();
        showLoading(false);
        if (documents.isEmpty())
        {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
        else
        {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void handlePermissionDenied()
    {
        Toast.makeText(this, R.string.simple_doc_permission_denied, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean loading)
    {
        progressView.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading)
        {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
        }
    }

    private List<File> collectDocuments()
    {
        List<File> results = new ArrayList<File>();
        for (File root : getCandidateRoots())
        {
            collectFromDirectory(root, results, 0);
            if (results.size() >= MAX_RESULTS)
            {
                break;
            }
        }
        return results;
    }

    private void collectFromDirectory(File directory, List<File> results, int depth)
    {
        if (!isReadableDir(directory))
        {
            return;
        }
        File[] children = directory.listFiles();
        if (children == null)
        {
            return;
        }
        for (File child : children)
        {
            if (child.isDirectory())
            {
                if (shouldSkipDir(child, depth))
                {
                    continue;
                }
                collectFromDirectory(child, results, depth + 1);
                if (results.size() >= MAX_RESULTS)
                {
                    return;
                }
            }
            else if (isSupported(child.getName()))
            {
                results.add(child);
                if (results.size() >= MAX_RESULTS)
                {
                    return;
                }
            }
        }
    }

    private boolean isReadableDir(File directory)
    {
        return directory != null && directory.exists() && directory.isDirectory() && directory.canRead();
    }

    private boolean shouldSkipDir(File dir, int depth)
    {
        if (dir == null)
        {
            return true;
        }
        String name = dir.getName();
        if (name.startsWith(".") || "Android".equalsIgnoreCase(name))
        {
            return true;
        }
        return depth > 8;
    }

    private boolean isSupported(String fileName)
    {
        if (fileName == null)
        {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.getDefault());
        for (String ext : SUPPORTED_EXTENSIONS)
        {
            if (lower.endsWith(ext))
            {
                return true;
            }
        }
        return false;
    }

    private void openDocument(File file)
    {
        if (file == null || !file.exists())
        {
            Toast.makeText(this, R.string.toast_open_file_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AppActivity.class);
        intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, file.getAbsolutePath());
        startActivity(intent);
    }

    @SuppressLint("ObsoleteSdkInt")
    private List<File> getCandidateRoots()
    {
        Set<File> roots = new LinkedHashSet<File>();
        File primary = Environment.getExternalStorageDirectory();
        if (isReadableDir(primary))
        {
            roots.add(primary);
        }
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (isReadableDir(downloads))
        {
            roots.add(downloads);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (isReadableDir(documentsDir))
            {
                roots.add(documentsDir);
            }
        }
        return new ArrayList<File>(roots);
    }

    private final class DocumentAdapter extends BaseAdapter
    {
        private final List<File> data;

        private DocumentAdapter(List<File> data)
        {
            this.data = data;
        }

        @Override
        public int getCount()
        {
            return data.size();
        }

        @Override
        public File getItem(int position)
        {
            return data.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent)
        {
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.item_simple_document, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            File file = getItem(position);
            holder.nameView.setText(file.getName());
            holder.pathView.setText(file.getParent());
            return convertView;
        }
    }

    private static final class ViewHolder
    {
        private final TextView nameView;
        private final TextView pathView;

        private ViewHolder(View itemView)
        {
            nameView = itemView.findViewById(R.id.document_name);
            pathView = itemView.findViewById(R.id.document_path);
        }
    }
}

