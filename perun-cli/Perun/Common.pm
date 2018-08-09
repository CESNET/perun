package Perun::Common;

use Exporter;
@ISA = ('Exporter');
@EXPORT_OK = ('tableToPrint', 'printMessage', 'getSortingFunction', 'printTable');

use strict;
binmode STDOUT, ":utf8";

use Hash::Util;
use Scalar::Util 'blessed';

use Perun::beans::ExtSource;
use Perun::beans::Group;
use Perun::beans::Member;
use Perun::beans::Host;
use Perun::beans::User;
use Perun::beans::UserExtSource;
use Perun::beans::Vo;
use Perun::beans::Attribute;
use Perun::beans::AttributeDefinition;
use Perun::beans::Facility;
use Perun::beans::Owner;
use Perun::beans::Resource;
use Perun::beans::Service;
use Perun::beans::ServicesPackage;
use Perun::beans::ServiceAttributes;
use Perun::beans::Candidate;
use Perun::beans::RichMember;
use Perun::beans::Destination;
use Perun::beans::AuditMessage;
use Perun::beans::TaskResult;
use Perun::beans::Publication;
use Perun::beans::Author;
use Perun::beans::Authorship;
use Perun::beans::Category;
use Perun::beans::RichUser;
use Perun::beans::Status;
use Perun::beans::NotifReceiver;
use Perun::beans::NotifRegex;
use Perun::beans::NotifTemplateMessage;
use Perun::beans::NotifTemplate;
use Perun::beans::ContactGroup;
use Perun::beans::SecurityTeam;
use Perun::beans::BanOnResource;
use Perun::beans::BanOnFacility;
use Perun::beans::AttributeRights;

sub newEmptyBean
{
	my $class = shift;
	$class = ref $class if ref $class;

	return fields::new($class);
}

sub fromHash
{
	my $class = shift;
	$class = ref $class if ref $class;
	my $self = { };
	my $hash;

	if ((@_ == 1) && (ref($_[0]) eq 'HASH')) {
		$hash = $_[0];
	} else {
		my %hash = @_;
		$hash = \%hash;
	}

	while (my ($k, $v) = each %$hash) {
		$self->{'_'.$k} = $v;
	}

	bless ($self, $class);
}

sub getHash
{
	return { @_ } unless (@_ == 1);
	return $_[0] if ref $_[0];
	return undef;
}

sub callManagerMethod
{
	my $method = shift;
	my $type = shift;
	my $self = shift;
	my $hash = Perun::Common::getHash(@_);

	my $result = $self->{_agent}->call( $self->{_manager}, $method, $hash );

	if (substr($type, 0, 2) eq '[]')
	{
		my @arr;
		$type = substr($type, 2);

		if (substr($type, 0, 2) eq '[]') {
			$type = substr($type, 2);

			if (($type eq 'string') || ($type eq 'number') || ($type eq '') || ($type eq 'null')) {
				foreach (@$result) {
					my @arr2;

					foreach (@$_) {
						push(@arr2, $_);
					}

					push(@arr, \@arr2);
				}
			} else {
				foreach (@$result) {
					my @arr2;

					foreach (@$_) {
						{
							no strict 'refs';
							push(@arr2, &{"Perun::beans::".$type."::fromHash"}('Perun::beans::'.$type, $_));
						}
					}

					push(@arr, \@arr2);
				}
			}
		} elsif (($type eq 'string') || ($type eq 'number') || ($type eq '') || ($type eq 'null')) {
			foreach (@$result) {
				push(@arr, $_);
			}
		} else {
			foreach (@$result) {
				{
					no strict 'refs';
					push(@arr, &{"Perun::beans::".$type."::fromHash"}('Perun::beans::'.$type, $_));
				}
			}
		}

		return @arr;
	}
	else
	{
		if (($type eq 'string') || ($type eq 'number') || ($type eq '') || ($type eq 'null')) {
			return $result;
		} else {
			my $ret = eval('Perun::beans::'.$type.'->fromHash($result)');
			if ($@) {
				die $@;
			}
			return $ret;
		}
	}
}

#Convert Text::ASCIITable to string (for printing)
# tableToPrint(TABLE [,BATCHMODE])
#TABLE is the Text::ASCIITable object
#BATCHMODE is boolean. If it's set, this function use simplier format to generate output.
#                      If it isn't set, it's get from PERUN_BATCH environmental variable.
#
#Returns string.  (Do not print anything)
sub tableToPrint {
	my $table = shift;
	my $batch = shift || $ENV{'PERUN_BATCH'};

	if ($batch) {
		$table->setOptions( 'hide_HeadLine', 1 );
		$table->setOptions( 'hide_HeadRow', 1 );
		$table->setOptions( 'hide_FirstLine', 1 );
		$table->setOptions( 'hide_LastLine', 1 );

		return $table->draw( [ '', '', '-', '+' ], #very first line           LEFT, RIGTH, LINE, DELIMITER
			[ '-', '', '|' ], #head row                  LEFT, RIGTH, DELIMITER
			[ '-', '', '-', '+' ], #heading separator         LEFT, RIGTH, LINE, DELIMITER
			[ ' ', '', '|' ], #row                       LEFT, RIGTH, DELIMITER
			[ '', '', '-', '+' ]     #very last line            LEFT, RIGTH, LINE, DELIMITER
			#(optional) row separator  LEFT, RIGTH, LINE, DELIMITER
		);
	} else {
		return $table->draw();
	}
}

#Print message unless batch mode is activated
#Act as build-in print function, except last parametr muset be boolean BatchMode
#Batch mode is also active if PERUN_BATCH environmental variable is set
sub printMessage {
	my $batch = pop || $ENV{'PERUN_BATCH'};
	unless ($batch) { print @_, "\n"; }
}

# Return sortingFunction which can be used as parameter for sort
# First argument is name of the method which will be called on sorted object during sorting. Object are sorted accordingly to output of this function.
# Second argument afect the sorting behavior.
#     If it's ommited or it's 0 the function compare values as numbers.
#     If it's equals 1 function compare valeus as strings - case-insesitive.
# Reverse sort order if third argument is true. If it is false or ommited sort ascendingly.
#
# Usage:
# my @vos = $vosAgent->getVos;
# my $sortingFunction = getSortingFunction("getShortName", 1);
# for my $vo (sort $sortingFunction @vos)  { DO SOMETHING }
sub getSortingFunction {
	my $method = shift;
	my $sortAsStrings = shift;
	my ($first, $second) = shift() ? (1, 0) : (0, 1);  # reverse sort order if 3rd agrument is true

	if ($sortAsStrings) {
		return sub ($$) {uc($_[$first]->$method) cmp uc($_[$second]->$method) }
	} else {
		return sub ($$) {$_[$first]->$method <=> $_[$second]->$method }
	}
}

sub printTable($@) {
	my $sortingFunction = shift;
	my @objects = @_;
	unless (@objects) {
		warn "Nothing to print.";
		return
	}

	if (!blessed($objects[0])) {
		warn "print_table: Can't print. Second argument is not an object.";
		return;
	}

	if (!$objects[0]->can( "getCommonArrayRepresentation" ) || !$objects[0]->can( "getCommonArrayRepresentationHeading" )) {
		warn "Required method getCommonArrayRepresentation or getCommonArrayRepresentationHeading is not supported by printed objects.";
		return;
	}

	my $table = Text::ASCIITable->new( { reportErrors => 0, utf8 => 0 } );
	$table->setCols( $objects[0]->getCommonArrayRepresentationHeading );

	for(sort $sortingFunction @objects) {
		$table->addRow( $_->getCommonArrayRepresentation );
	}

	print tableToPrint($table, $::batch);
}

1;
