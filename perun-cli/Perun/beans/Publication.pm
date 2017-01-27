package Perun::beans::Publication;

use strict;
use warnings;

use Perun::Common;

use overload
	'""' => \&toString;

sub toString {
	my $self = shift;

	my @authors = $self->{_authors};
	my $rank = $self->{_rank};
	my $id = $self->{_id};
	my $externalId = $self->{_externalId};
	my $publicationSystemId = $self->{publicationSystemId};
	my $title = $self->{_title};
	my $year = $self->{_year};
	my $main = $self->{_main};
	my $isbn = $self->{_isbn};
	my $categoryId = $self->{_categoryId};
	my $createdBy = $self->{_createdBy};
	my $createdDate = $self->{_createdDate};
	my $doi = $self->{_doi};
	my $locked = $self->{_locked};

	my $str = 'Publication (';
	$str .= "authors: @authors, " if (@authors);
	$str .= "rank: $rank, " if ($rank);
	$str .= "id: $id" if ($id);
	$str .= "externalId: $externalId" if ($externalId);
	$str .= "publicationSystemId: $publicationSystemId" if ($publicationSystemId);
	$str .= "title: $title" if ($title);
	$str .= "year: $year" if ($year);
	$str .= "main: $main" if ($main);
	$str .= "isbn: $isbn" if ($isbn);
	$str .= "categoryId: $categoryId" if ($categoryId);
	$str .= "createdBy: $createdBy" if ($createdBy);
	$str .= "createdDate: $createdDate" if ($createdDate);
	$str .= "doi: $doi" if ($doi);
	$str .= "locked: $locked" if ($locked);
	$str .= ')';

	return $str;
}

sub new
{
	bless({});
}

sub fromHash
{
	my $publication = Perun::Common::fromHash(@_);
	for my $author (@{$publication->{_authors}}) {
		$author = Perun::beans::Author::fromHash("Perun::beans::Author", $author);
	}
	return $publication;
}

sub TO_JSON
{
	my $self = shift;

	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id} * 1;
	} else {
		$id = 0;
	}

	my $title;
	if (defined($self->{_title})) {
		$title = "$self->{_title}";
	} else {
		$title = undef;
	}

	my $rank;
	if (defined($self->{_rank})) {
		$rank = $self->{_rank} * 1;
	} else {
		$rank = 0;
	}

	my $externalId;
	if (defined($self->{_externalId})) {
		$externalId = $self->{_externalId} * 1;
	} else {
		$externalId = 0;
	}

	my $publicationSystemId;
	if (defined($self->{_publicationSystemId})) {
		$publicationSystemId = $self->{_publicationSystemId} * 1;
	} else {
		$publicationSystemId = 0;
	}

	my $year;
	if (defined($self->{_year})) {
		$year = $self->{_year} * 1;
	} else {
		$year = undef;
	}

	my $categoryId;
	if (defined($self->{_categoryId})) {
		$categoryId = $self->{_categoryId} * 1;
	} else {
		$categoryId = 0;
	}

	my $main;
	if (defined($self->{_main})) {
		$main = "$self->{_main}";
	} else {
		$main = undef;
	}

	my $isbn;
	if (defined($self->{_isbn})) {
		$isbn = "$self->{_isbn}";
	} else {
		$isbn = undef;
	}

	my $createdBy;
	if (defined($self->{_createdBy})) {
		$createdBy = "$self->{_createdBy}";
	} else {
		$createdBy = undef;
	}

	my $createdDate;
	if (defined($self->{_createdDate})) {
		$createdDate = "$self->{_createdDate}";
	} else {
		$createdDate = undef;
	}

	my $doi;
	if (defined($self->{_doi})) {
		$doi = "$self->{_doi}";
	} else {
		$doi = undef;
	}

	my $locked;
	if (defined($self->{_locked})) {
		$locked = $self->{_locked} * 1;
	} else {
		$locked = 0;
	}

	my @authors;
	if (defined($self->{_authors})) {
		@authors = $self->{_authors} * 1;
	} else {
		@authors = undef;
	}

	return { id                    => $id, title => $title, rank => $rank, externalId => $externalId,
		publicationSystemId        => $publicationSystemId, year => $year, isbn => $isbn, main => $main, categoryId =>
		$categoryId, createdBy     => $createdBy, createdDate => $createdDate, doi => $doi, locked => $locked, authors
								   => \@authors };
}

sub getId
{
	my $self = shift;

	return $self->{_id};
}

sub setId
{
	my $self = shift;
	$self->{_id} = shift;

	return;
}

sub getTitle
{
	my $self = shift;

	return $self->{_title};
}

sub setTitle
{
	my $self = shift;
	$self->{_title} = shift;

	return;
}

sub getRank
{
	my $self = shift;

	return $self->{_rank};
}

sub setRank
{
	my $self = shift;
	$self->{_rank} = shift;

	return;
}

sub getExternalId
{
	my $self = shift;

	return $self->{_externalId};
}

sub setExternalId
{
	my $self = shift;
	$self->{_externalId} = shift;

	return;
}

sub getPublicationSystemId
{
	my $self = shift;

	return $self->{_publicationSystemId};
}

sub setPublicationSystemId
{
	my $self = shift;
	$self->{_publicationSystemId} = shift;

	return;
}

sub getYear
{
	my $self = shift;

	return $self->{_year};
}

sub setYear
{
	my $self = shift;
	$self->{_year} = shift;

	return;
}

sub getMain
{
	my $self = shift;

	return $self->{_main};
}

sub setMain
{
	my $self = shift;
	$self->{_main} = shift;

	return;
}

sub getIsbn
{
	my $self = shift;

	return $self->{_isbn};
}

sub setIsbn
{
	my $self = shift;
	$self->{_isbn} = shift;

	return;
}

sub getCategoryId
{
	my $self = shift;

	return $self->{_categoryId};
}

sub setCategoryId
{
	my $self = shift;
	$self->{_categoryId} = shift;

	return;
}

sub getCategoryName
{
	my $self = shift;

	return $self->{_categoryName};
}

sub setCategoryName
{
	my $self = shift;
	$self->{_categoryName} = shift;

	return;
}

sub getCreatedBy
{
	my $self = shift;

	return $self->{_createdBy};
}

sub setCreatedBy
{
	my $self = shift;
	$self->{_createdBy} = shift;

	return;
}

sub getCreatedDate
{
	my $self = shift;

	return $self->{_createdDate};
}

sub setCreatedDate
{
	my $self = shift;
	$self->{_createdDate} = shift;

	return;
}

sub getDoi
{
	my $self = shift;

	return $self->{_doi};
}

sub setDoi
{
	my $self = shift;
	$self->{_doi} = shift;

	return;
}

sub getLocked
{
	my $self = shift;

	return $self->{_locked};
}

sub setLocked
{
	my $self = shift;
	$self->{_locked} = shift;

	return;
}

sub getAuthors
{
	my $self = shift;

	return @{$self->{_authors}};
}
#return '["' . join('", "', @$value) . '"]'
sub printAuthors
{
	my $self = shift;
	my @authors = $self->getAuthors;
	my @result;
	foreach my $author (@authors) {
		my $name = "";
		if ($author->getLastName) {
			$name = $name . $author->getLastName;
			if ($author->getFirstName) {
				$name = $name . ' ' . $author->getFirstName;
			}
		} else {
			if ($author->getFirstName) {
				$name = $name . $author->getFirstName;
			}
		}
		push @result, $name;
	}
	return join(', ', sort @result);
}

sub getCommonArrayRepresentation {
	my $self = shift;
	return ($self->getId, $self->getYear, $self->getTitle, $self->printAuthors, $self->getCategoryName, $self->getRank, $self->getLocked);
}

sub getCommonArrayRepresentationHeading {
	return ('ID', 'Year', 'Name', 'Authors', 'Category', 'Rank', 'Locked');
}

1;
