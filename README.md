# Indexed-sequential files
Simple implementation of indexed-sequential file organisation with simulated page reading.
It contains records representing parallelograms, sorted by key value.

This project was written as an assignment for SBD(Struktury Baz Danych/ Database Structures) class.

## Console mode

This program works in simplistic console mode. It can be set to (create and)read files with prepared sets of commands.
Behaviour of the program is hardcoded in the Main class.

> The InteractiveMode class will scan input like so:
>
>     command
>
>     arg1, arg2, ...
>
>     command
>
>     arg1, arg2 ...
>
>     ...

### Commands

newFile ; path - creates new IdxSeqFile(path) - there is no file by default, it has to be created before other commands can be executed.

add ; ... - saves specified record to the IdxSeqFile

get ; key - gets record from the file and prints it to the console

printFile - prints out the IdxSeqFile

reorg - reorganises the IdxSeqFile

counts - get the number of disc operations
