#include "mainwindow.h"
#include <QApplication>
#include <QTextCodec>
#include <QTextCodec.h>
int main(int argc, char *argv[])
{
    QTextCodec* codec = QTextCodec::codecForName("UTF-8");
    QTextCodec::setCodecForLocale(codec);
   // QTextCodec::setCodecForCStrings(codec);
    setlocale(LC_ALL,"");

    QApplication a(argc, argv);
    MainWindow w;
    w.show();

    return a.exec();
}
