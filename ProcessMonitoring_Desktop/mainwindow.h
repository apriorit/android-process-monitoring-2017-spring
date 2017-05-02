#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QtNetwork/QNetworkAccessManager>
#include <QTextStream>
#include <QFile>
#include <QUrl>
#include <QtNetwork/QNetworkRequest>
#include <QString>
#include <QHttpMultiPart>
#include <QHostAddress>
#include <QNetworkReply>
#include <QPushButton>
#include <QLabel>
#include <QQuickView>
#include <QQuickItem>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private:
    Ui::MainWindow *ui;
    QPushButton *btnGetListDevices;
    QPushButton *btnSendRequest;
    QPushButton *btnSetCoordinates;
    //This class allows the application to send network requests and receive replies
    QNetworkAccessManager* manager;
    QLabel *label;
    QQuickView *view;
    QQuickItem *item;

private slots:
    void Response(QNetworkReply* reply);
    void sendRequest(QString);
    void showMap();
    void listDevices();
};

#endif // MAINWINDOW_H
