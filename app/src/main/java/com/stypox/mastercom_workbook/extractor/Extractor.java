package com.stypox.mastercom_workbook.extractor;

import android.os.Handler;
import android.os.Looper;

import com.stypox.mastercom_workbook.data.ClassData;
import com.stypox.mastercom_workbook.data.DocumentData;
import com.stypox.mastercom_workbook.data.StudentData;
import com.stypox.mastercom_workbook.data.SubjectData;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class Extractor {

    public interface DataHandler<D> {
        void onExtractedData(D data);
        void onItemError(ExtractorError error);
        void onError(ExtractorError error);
    }

    public interface ItemErrorHandler {
        void onItemError(Throwable throwable);
    }

    private static final String APIUrlToShow = "{api_url}.registroelettronico.com";

    private static String APIUrl;
    private static String user;
    private static String password;

    // extracted data
    private static List<SubjectData> subjects;
    private static StudentData student;


    /////////////
    // GETTERS //
    /////////////

    public static void extractSubjects(boolean forceReload,
                                       CompositeDisposable disposables,
                                       DataHandler<List<SubjectData>> handler) {
        if (forceReload || subjects == null) {
            disposables.add(SubjectExtractor
                    .fetchSubjects(t -> signalItemErrorOnMainThread(t, handler))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> {
                                subjects = data;
                                handler.onExtractedData(data);
                            },
                            throwable -> handler.onError((ExtractorError) throwable)));
        } else {
            handler.onExtractedData(subjects);
        }
    }

    public static void extractMarks(SubjectData subject,
                                    boolean forceReload,
                                    CompositeDisposable disposables,
                                    DataHandler<SubjectData> handler) {
        if (forceReload || subject.getMarks() == null) {
            disposables.add(SubjectExtractor
                    .fetchMarks(subject, t -> signalItemErrorOnMainThread(t, handler))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(handler::onExtractedData,
                            throwable -> handler.onError((ExtractorError) throwable)));
        } else {
            handler.onExtractedData(subject);
        }
    }

    public static void extractTopics(SubjectData subject,
                                     boolean forceReload,
                                     CompositeDisposable disposables,
                                     DataHandler<SubjectData> handler) {
        if (forceReload || subject.getTopics() == null) {
            disposables.add(SubjectExtractor
                    .fetchTopics(subject, t -> signalItemErrorOnMainThread(t, handler))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(handler::onExtractedData,
                            throwable -> handler.onError((ExtractorError) throwable)));
        } else {
            handler.onExtractedData(subject);
        }
    }

    public static void extractStudent(boolean forceReload,
                                      CompositeDisposable disposables,
                                      DataHandler<StudentData> handler) {
        if (forceReload || student == null) {
            disposables.add(StudentExtractor
                    .fetchStudent(t -> signalItemErrorOnMainThread(t, handler))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> {
                                student = data;
                                handler.onExtractedData(student);
                            },
                            throwable -> handler.onError((ExtractorError) throwable)));
        } else {
            handler.onExtractedData(student);
        }
    }

    public static void extractDocuments(ClassData classData,
                                         boolean forceReload,
                                         CompositeDisposable disposables,
                                         DataHandler<ClassData> handler) {
        if (forceReload || classData.getDocuments() == null) {
            disposables.add(DocumentExtractor
                    .fetchDocuments(classData, t -> signalItemErrorOnMainThread(t, handler))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(handler::onExtractedData,
                            throwable -> handler.onError((ExtractorError) throwable)));
        } else {
            handler.onExtractedData(classData);
        }
    }


    ////////////////////
    // AUTHENTICATION //
    ////////////////////

    public static void setAPIUrl(String APIUrl) {
        Extractor.APIUrl = APIUrl;
    }

    public static void setUser(String user) {
        Extractor.user = user;
    }

    public static void setPassword(String password) {
        Extractor.password = password;
    }


    static String getAPIUrl() {
        return APIUrl;
    }

    static String getUser() {
        return user;
    }

    static String getPassword() {
        return password;
    }


    ////////////
    // RANDOM //
    ////////////

    /**
     * @return a user-friendly representation of the API url
     */
    public static String getFullAPIUrlToShow() {
        return APIUrlToShow.replace("{api_url}", APIUrl);
    }

    /**
     * To be used ONLY to obtain subjects in activities where it is obvious that they have already
     * been extracted.
     * @return the already extracted SubjectData
     */
    public static List<SubjectData> getExtractedSubjects() {
        return subjects;
    }


    /////////////
    // HELPERS //
    /////////////

    private static void signalItemErrorOnMainThread(Throwable throwable, DataHandler dataHandler) {
        // json has surely been already parsed: the extractor was looping through it to get items
        ExtractorError error = ExtractorError.asExtractorError(throwable, true);
        runOnMainThread(() -> dataHandler.onItemError(error));
    }

    private static void runOnMainThread(Runnable runnable) {
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        mainThreadHandler.post(runnable);
    }
}
