#!/usr/local/bin/perl

use Data::Dumper;
use LWP::Simple;

my %count = ();
my %context = ();

while(<>) {
	chomp;
	lc($_);
	my @tokens = split("[ ]+");
	my @ttokens = ();
	for my $t (@tokens) {
		my @tags = split("_", $t);
		if($tags[1] =~ /NN/) {
			push @ttokens, lc($tags[0]);
			$count{lc($tags[0])} += 1;
			if(!exists $context{lc($tags[0])}) {
				$context{lc($tags[0])} = ();
			}
		}
	}

	# context
	for(my $i=0; $i<@ttokens; $i++) {
		for(my $j=0; $j<@ttokens; $j++) {
			if($i != $j) {
				push @{$context{$ttokens[$i]}}, $ttokens[$j];
			}
		}
	}
}

for my $t (keys %count) {
	if($count{$t} >= 5) {
		print "Term: $t\n";
		for my $c (@{$context{$t}}) {
			my $senses = getSenses($t, $c);
			if($senses) {
				print $senses->[0]->{'title'} . " [" . $senses->[0]->{'id'} . "]" . "\t" . $senses->[1]->{'title'} . " [" . $senses->[1]->{'id'} . "]" . "\n";
			}
		}
	}
}


sub getSenses {
	my ($t1, $t2) = @_;
	my @senses = ();
	my $content = get("http://wdm.cs.waikato.ac.nz:8080/service?task=compare&details=true&term1=$t1&term2=$t2&xml");
	my @tokens = split("Sense1", $content);
	if(@tokens >= 2) {
		@tokens = split("\"", $tokens[1]);
		push @senses, {'id' => $tokens[3], 'title' => $tokens[5]};
		@tokens = split("Sense2", $content);
		if(@tokens >= 2) {
			@tokens = split("\"", $tokens[1]);
			push @senses, {'id' => $tokens[3], 'title' => $tokens[5]};
		}
	} else {
		return undef;
	}

	return \@senses;
}
