package ScriptLock;
use strict;
use warnings FATAL => 'all';
use Fcntl qw(:flock);

use constant {
	MAIN_LOCK_DIR => "./",
};

sub new
{
	my $class = shift;
	my $lockName = shift;
	unless(defined($lockName)) { die "Can't create ScriptLock object without definition of lockName!\n"; }
	my $self = bless {}, $class;
	$self->{_lockPath} = MAIN_LOCK_DIR . "/" . $lockName . ".lock";

	return $self;
}

sub getLockPath
{
	my $self = shift;
	return $self->{_lockPath};
}

sub getLock
{
	my $self = shift;
	return $self->{_lock};
}

sub lock {
	my $self = shift;
	return 0 unless(open($self->{_lock}, ">", $self->{_lockPath}));
	return 0 unless(flock($self->{_lock}, LOCK_EX|LOCK_NB));
	return 1;
}

sub unlock {
	my $self = shift;
	unless(defined($self->{_lock})) {
		$! = "Lock not exists, probably need to be created first.";
		return 0;
	}
	return 0 unless(flock($self->{_lock}, LOCK_UN|LOCK_NB));
	return 1;
}

1;