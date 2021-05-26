## Compile source
```console
user@Server$ ./compile.sh
```

## Launch collection server
```console
user@Server/bin$ java -classpath $(for f in ../lib/*; do printf "$f:"; done) 'soup.Collection$Server'
```

## Launch streaming server
```console
user@Server/bin$ LIBS=$(for f in ../lib/*; do printf "$f;"; done); powershell.exe "java.exe -classpath \"$LIBS\" 'soup.Streaming\$Server'"
```
