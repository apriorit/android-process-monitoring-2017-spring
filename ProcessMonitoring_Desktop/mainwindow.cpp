#include "mainwindow.h"
#include "ui_mainwindow.h"

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    btnSendRequest = new QPushButton("Send request", this);
    btnSendRequest->setGeometry(QRect(QPoint(10, 10), QSize(100, 30)));

    btnSetCoordinates = new QPushButton("Set coordinates", this);
    btnSetCoordinates->setGeometry(QRect(QPoint(150, 10), QSize(100, 30)));
    btnSetCoordinates->setCheckable(true);

    connect(btnSetCoordinates, SIGNAL(released()), this, SLOT(showMap()));

    //connect the signal to the appropriate slot
    connect(btnSendRequest, SIGNAL (released()), this, SLOT (sendRequest()));
    manager = new QNetworkAccessManager(this);

    label = new QLabel(this);
    label->setText("");
    label->setGeometry(QRect(QPoint(10, 100), QSize(350, 30)));

    //placing map in main window
    view = new QQuickView();
    QWidget *container = QWidget::createWindowContainer(view, this);
    //set properties of container with map
    container->setGeometry(QRect(QPoint(300, 20), QSize(540, 500)));
    container->setFocusPolicy(Qt::TabFocus);

     QUrl q(QStringLiteral("QML:///map.qml"));
    view->setSource(QUrl(QStringLiteral("qrc:///map.qml")));
    item = view->rootObject();

    //set coordinates specifying the center of the viewport
    item->setProperty("latitude", 48.4656371);
    item->setProperty("longitude",  35.04900455);

    //coordinates of the marker on this map
    item->setProperty("device_latitude", 48.48508294);
    item->setProperty("device_longitude", 35.08914748);
}

//Handles response from our App server
void MainWindow::Response(QNetworkReply *reply){
    QByteArray data = reply->readAll();
    QString response = QString::fromUtf8(data);
    qDebug() << response;
    label->setText(response);

    //parse json string
    QJsonDocument jsonResponse = QJsonDocument::fromJson(response.toUtf8());
    QJsonObject jsonObjTemp = jsonResponse.object();
    QJsonObject::iterator it;

    for (it = jsonObjTemp.begin(); it != jsonObjTemp.end(); it++) {
          QString package = it.key();
          QString appName =  it.value().toString();
          qDebug() << package  << "  " << appName;
      }
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
void MainWindow::showMap() {
    //change location of marker
    item->setProperty("device_latitude", 48.47036766);
    item->setProperty("device_longitude", 35.03392518);
}

MainWindow::~MainWindow()
{
    delete ui;
}
