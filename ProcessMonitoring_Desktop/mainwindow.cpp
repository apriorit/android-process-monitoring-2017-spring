#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "QTextCodec"
MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    btnSendRequest = new QPushButton("Send request", this);
    btnSendRequest->setGeometry(QRect(QPoint(10, 10), QSize(100, 30)));

    //connect the signal to the appropriate slot
    connect(btnSendRequest, SIGNAL (released()), this, SLOT (sendRequest()));
    manager = new QNetworkAccessManager(this);

    label = new QLabel(this);
    label->setText("");
    label->setGeometry(QRect(QPoint(10, 100), QSize(350, 30)));
}
//Handles response from our App server
void MainWindow::Response(QNetworkReply *reply){
    QByteArray data = reply->readAll();
    QString response = QString::fromUtf8(data);
    qDebug() << response;
    label->setText(response);
}
//Sends message to our App server
void MainWindow::sendRequest() {
    QUrl url("http://127.0.0.1:8000"); //192.168.0.101
    url.port(8000);

    QByteArray postData;
    QNetworkRequest qNetworkRequest(url);
    qNetworkRequest.setHeader(QNetworkRequest::ContentTypeHeader, "application/x-www-form-urlencoded");

    QString request("list-apps");
    postData.append(request);
    QObject::connect(manager, SIGNAL(finished(QNetworkReply *)), this, SLOT(Response(QNetworkReply *)));

    manager->post(qNetworkRequest, postData);
}
MainWindow::~MainWindow()
{
    delete ui;
}
