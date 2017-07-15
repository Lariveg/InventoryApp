package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.StoreContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;

    View emptyView;

    ListView productListView;

    //Setup the sale button
    public void onSalePress(View view) {
        ContentResolver resolver = getContentResolver();

        Button saleButton = (Button) view;
        int currentProductId = Integer.parseInt(saleButton.getTag().toString());

        Uri uri = Uri.parse("content://com.example.android.inventoryapp/inventory/"
                + String.valueOf(currentProductId));

        Cursor cursor = resolver.query(uri, null, null, null, null);

        String quantityValue = null;
        int quantity = 0;
        if (cursor != null && cursor.moveToFirst()){
            System.out.println("Never executed");
            int quantityColumnIndex = 0;
            quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            quantityValue = cursor.getString(quantityColumnIndex);
            quantity = Integer.parseInt(quantityValue);
            cursor.close();
        }

        ContentValues values = new ContentValues();

        // subtract one from the quantity and display in the TextView
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);

        // It doesn't make sense to have a negative quantity, so...
        if (quantity <= 0) {
            // inform the user
            Toast.makeText(this, getString(R.string.no_negative_quantity),
                    Toast.LENGTH_SHORT).show();

            // set the quantity to 0 and display
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, 0);

        }

        resolver.update(uri, values, null, null);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open InsertActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //Setup the adapter to create a list item for each row of product data in the Cursor.
        //There is no product data yet (until loader finished) so pass in null for the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@link Detail Activity}
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                //Form the content URI that represents the specific product that was clicked on
                Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                //Launch the DetailActivity
                startActivity(intent);
            }
        });

        //Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns of the table that we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY
        };

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, //Parent activity context
                InventoryEntry.CONTENT_URI, //Provider Content URI to query
                projection, //Columns to include in the resulting Cursor
                null, //No selection clause
                null, // No selection arguments
                null); //Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update ProductCursorAdapter with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
        if (emptyView != null) {
            emptyView.setVisibility(data != null && data.getCount() > 0 ? View.GONE : View.VISIBLE);
            productListView.setVisibility(data != null && data.getCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
