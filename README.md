# pad-lab-1 (Agent de mesaje)
Acest repositoriu conține implementarea lucrării nr.1 la programarea aplicațiilor distribuite.
Condiția poate fi găsită [aici](https://github.com/Alexx-G/PAD-labs/blob/master/PAD-lab1.md)
##### *Sarcini implemenate*
- Implementarea unei cozi de mesaje
- Implementarea mecanismului de rutare a mesajelor
-  Implementarea patternului de publisher-subscriber
- Implementarea rutării avansate a mesajelor

### Rularea proiectului
Pentru a porni serverul, trebuie de trecut din linia de comandă `cmd` în directoriul *broker-server*, și de executat comanda:
```
mvn exec:java -Dexec.mainClass="md.utm.pad.labs.broker.demo.Main"
```
Astfel serverul va fi pornit. Stoparea lui se face prin tastarea a oricărei taste în linia de comandă.
Pentru a trimite un mesaj,  trebuie de trecut din linia de comandă `cmd` în directoriul *broker-client*, și de executat comanda:
```
mvn exec:java -Dexec.mainClass="md.utm.pad.labs.broker.client.demo.Sender" -Dexec.args="<PAYLOAD>'"
```
Pentru a primi un mesaj în mod sincron, trebuie de trecut din linia de comandă `cmd` în directoriul *broker-client*, și de executat comanda:
```
mvn exec:java -Dexec.mainClass="md.utm.pad.labs.broker.client.demo.Receiver"
```
Comanda de mai sus va citi un mesaj dintr-o coadă și se va opri.

Pentru a primi un mesaj în mod asincron, trebuie de trecut din linia de comandă `cmd` în directoriul *broker-client*, și de executat comanda:
```
mvn exec:java -Dexec.mainClass="md.utm.pad.labs.broker.client.demo.AsyncReceiver"
```
Comanda de mai sus va citi un mesaje din coadă în mod asincron până ce nu va fi stopată. Stoparea se face cu ajutorul culegerii a oricărei taste.
