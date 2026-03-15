# Indexed-sequential files
Implementation of indexed-sequential file organisation with simulated page reading.
It contains records representing parallelograms, sorted by key value.

This project was written as an assignment for SBD(Struktury Baz Danych/ Database Structures) class.

## About Indexed-Sequential files.

Indexed-Sequential files are a complex file organisation.

It improves over the basic sequential file organisation by:
- providing faster associative access.
- providing support for inserting new records(faster).

Those things are achieved by storing a list of first keys on each disk page, which is faster to search through than the main area 
and an overflow area, which allows us to add new records "in between" already stored records without reorganisation of the whole main area.

## Console mode

This program works in mode. 

It can be set to (create and)read files with prepared sets of commands.
Behaviour of the program is hardcoded in the Main class.

> The InteractiveMode class will scan input like so:
>
>     >command
>     arg1, arg2, ...
>     >command
>     arg1, arg2 ...
>     ...

### Commands

newFile ; path - creates new IdxSeqFile(path) - **there is no file by default**, it has to be created before other commands can be executed.

add ; ... - saves specified record to the IdxSeqFile

get ; key - gets record from the file and prints it to the console

printFile - prints out the IdxSeqFile

reorg - reorganises the IdxSeqFile

counts - get the number of disc operations
