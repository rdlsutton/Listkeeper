package com.radlsuttonedmn.listkeeper;

import android.app.Activity;
import android.net.Uri;
import androidx.fragment.app.FragmentActivity;
import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Retrieve a list of files from the storage directory
 */

class FilesList {

    private final FragmentActivity mActivity;

    // Constructor with the activity as a parameter
    FilesList(Activity activity) {

        mActivity = (FragmentActivity)activity;
    }

    // Validate the header row in the selected file
    private boolean validHeader(String[] header, ListDefinition listDefinition) {

        String importField;

        // Do not process a null header
        if (header == null) {
            return true;
        }

        // Verify that the number of fields in the header is correct
        if (header.length != listDefinition.getFieldCount()) {
            return false;
        }

        // Verify that the field names match
        for (int i = 0; i < header.length; i++) {

            // Excel can produce strange characters and they need to be eliminated
            importField = header[i].trim().replaceAll("[^\\x00-\\x7F]", "");
            if (!importField.equals(listDefinition.getField(i).getFieldName())) {
                return false;
            }
        }
        return true;
    }

    // Import the selected file
    String importFile(String fileUriString, StoredList storedList, ListDefinition listDefinition) {

        String[] line;
        int linesDuplicate = 0;
        int linesFailed = 0;
        int linesInvalidDate = 0;
        int linesInvalidTime = 0;
        int linesInvalidNumber = 0;
        int linesInvalidPhone = 0;
        int linesRead = 0;
        List<String> itemRecord;
        boolean blankLine;

        StringBuilder results = new StringBuilder();
        try {
            Uri fileUri = Uri.parse(fileUriString);
            InputStream inputStream = mActivity.getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                CSVReader csvReader = new CSVReader(inputStreamReader);

                if (validHeader(csvReader.readNext(), listDefinition)) {
                    while ((line = csvReader.readNext()) != null) {
                        itemRecord = new ArrayList<>();
                        Collections.addAll(itemRecord, line);

                        // Check for and skip any blank lines
                        blankLine = true;
                        for (String item : itemRecord) {
                            if (!item.trim().isEmpty()) {
                                blankLine = false;
                            }
                        }
                        if (blankLine) {
                            continue;
                        }

                        // Add each line from the file to the stored list
                        switch (storedList.addImportedRecord(itemRecord, listDefinition)) {
                            case DUPLICATE_ITEM:
                                linesDuplicate++;
                                break;
                            case INVALID_DATE:
                                linesInvalidDate++;
                                break;
                            case INVALID_NUMBER:
                                linesInvalidNumber++;
                                break;
                            case INVALID_PHONE:
                                linesInvalidPhone++;
                                break;
                            case INVALID_TIME:
                                linesInvalidTime++;
                                break;
                            case ITEM_SAVED:
                                linesRead++;
                                break;
                            case STORE_ERROR:
                                linesFailed++;
                                break;
                        }
                    }
                } else {
                    results.append(mActivity.getString(R.string.bad_header));
                }
                inputStream.close();
                csvReader.close();
            } else {
                results.append(mActivity.getString(R.string.file_read_error));
            }
        } catch (FileNotFoundException e) {
            results.append(mActivity.getString(R.string.file_not_found));
        } catch (IOException e) {
            results.append(mActivity.getString(R.string.file_read_error));
        }

        // If there were no errors, report complete success
        if (linesDuplicate == 0 && linesFailed == 0 && linesInvalidDate == 0 && linesInvalidNumber == 0 && linesInvalidPhone == 0 &&
                        linesInvalidTime == 0 && results.toString().equals("")) {
            results.append(mActivity.getString(R.string.import_complete));
        } else {

            // There were import errors, count and report the number of errors
            if (linesRead > 0) {
                results.append(linesRead);
                results.append(" ");
                results.append(mActivity.getString(R.string.lines_imported));
                results.append("\n");
            }
            if (linesDuplicate > 0) {
                results.append(linesDuplicate);
                results.append(" ");
                results.append(mActivity.getString(R.string.lines_duplicate));
                results.append("\n");
            }
            if (linesInvalidDate > 0) {
                results.append(linesInvalidDate);
                results.append(" ");
                results.append(mActivity.getString(R.string.lines_invalid_date));
                results.append("\n");
            }
            if (linesInvalidNumber > 0) {
                results.append(linesInvalidNumber);
                results.append(" ");
                results.append(mActivity.getString(R.string.lines_invalid_number));
                results.append("\n");
            }
            if (linesInvalidPhone > 0) {
                results.append(linesInvalidPhone);
                results.append(" ");
                results.append(mActivity.getString(R.string.lines_invalid_phone));
                results.append("\n");
            }
            if (linesInvalidTime > 0) {
                results.append(linesInvalidTime);
                results.append(" ");
                results.append(mActivity.getString(R.string.lines_invalid_time));
                results.append("\n");
            }
            if (linesFailed > 0) {
                results.append(linesFailed);
                results.append(" ");
                results.append(mActivity.getString(R.string.import_fail));
            }
        }
        return results.toString();
    }
}
