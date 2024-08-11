# TP1

tp1 para simulacion de sistemas

## Usage

- The java project was made using maven so to run you should:

```bash

mvn clean install

```

```bash

mvn exec:java -Dexec.mainClass="ar.edu.itba.ss.App"

```

For now it doesn't offer parametrization, so you should change the values in the code. The json output should be copied to the plotter folder.

- The python project was made using pipenv so to run you should:

```bash
pipenv install
```

```bash
pipevn run python plotter.py
```

This will create a neighbours.png file in the plotter folder.
