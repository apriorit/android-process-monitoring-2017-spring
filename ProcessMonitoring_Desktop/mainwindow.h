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
    QPushButton *btnSendRequest;
    //This class allows the application to send network requests and receive replies
    QNetworkAccessManager* manager;
    QLabel *label;
private slots:
    void Response(QNetworkReply* reply);
    void sendRequest();
};

#endif // MAINWINDOW_H
