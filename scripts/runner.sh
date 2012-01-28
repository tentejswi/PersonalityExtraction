#!/bin/sh

procs=`ps -aux | grep -e 'p[e].jar' | wc -l`
echo $procs
if [ $procs -ne 0 ]
then
	echo "skipping - pe process already running"
else
	`java -jar /home/ubuntu/runnables/pe.jar >> /home/ubuntu/logs/pe.log`
fi
