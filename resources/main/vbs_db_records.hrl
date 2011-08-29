% vbs_db_records.erl
%% @author Jonathan Bernard <jdb@jdb-labs.com>

%% @doc
%%  The VBS database API is centered around the data records:
%% 
%%  * Tables are named after the records. ``vbs_adult`` records are stored in 
%%    a table named ``vbs_adult``.
%%  * The functions that make up the database API are grouped into modules named
%%    after the records on which they operate. The ``vbs_adult`` module contains
%%    the standard VBS database API functions that work with ``vbs_adult``
%%    records.
%%
%% @section Record Definitions
%% Here are the record definitions:

%% @doc Information about an adult in the VBS system. 
-record(vbs_adult, {

    %% @doc A unique number. This is the record's primary identification and
    %%  the table's primary key
    id,

    %% @doc A unique full name.
    %% @example "John Smith", "Fae Alice McDonald"
    name,

    %% @doc The adult's age (optional).
    age = 0,

    %% @doc A list of phone numbers (strings). 
    %% @example ["512-555-1155", "123-456-7890"]
    phone_numbers,

    %% @doc The adult's address (optional). There is not pre-defined format,
    %%      this is a string that can be formatted s desired (linebreaks are ok,
    %%      for example).
    %% @example
    %%  
    %%     "123 Grant Drive
    %%      Plainsville, TX, 78707"
    address = "",

    %% @doc The adult's email address as a string.
    %% @example "john_smith@mailco.com"
    email = ""}).

%% @doc An entry recording a person's attendance.
-record(vbs_attendance, {

    %% @doc A unique number. This is the record's primary identification and the
    %%  table's primary key.
    id,

    %% @doc The id of person who attended. This is a foreign key onto either the
    %%  [`vbs_worker`](doc://records/vbs_worker) or
    %%  [`vbs_child`](doc://records/vbs_child) table, depending on the value of
    %%  the [`person_type`](doc://records/vbs_attendance/person_type) field.
    person_id,

    %% @doc The type of person who attended. This determines which table the
    %%  [`person_id`](doc://records/vbs_attendance/person_id) links on. The
    %%  possible values and the corresponding link tables are:
    %%
    %%  ========  ========================================
    %%  `child`   [`vbs_child`](doc://records/vbs_child)
    %%  `worker`  [`vbs_worker`](doc://records/vbs_worker)
    %%  ========  ========================================
    %%
    person_type,

    %% @doc The date of attendance, stored as {Year, Month, Day}.
    %% @example {2011, 6, 14}
    date = {1900, 1, 1},

    %% @doc A timestamp taken when the person was signed in, stored  as
    %%  {Hour, Minute, Second}
    %% @example {5, 22, 13}
    sign_in = false,        % {hour, minute, second}

    %% @doc A timestamp taken when the person is signed out, stored as
    %% {Hour, Minute, Second}
    sign_out = false,       % {hour, minute, second}

    %% @doc A list of {Key, Value} pairs that can be used to store additional
    %%  information. This is intended to allow callers to store optional data,
    %%  or client-specific data, without having to alter the database schema.
    %%  When working with `vbs_attendance` records, a caller should ignore
    %%  `ext_data` values it does not understand
    ext_data = [],

    %% @doc Any comments for the day about this person.
    comments = ""}).

%% @doc Information about a child in the VBS program.
-record(vbs_child, {

    %% @doc A unique number. This is the record's primary identification and the
    %%  table's primary key.
    id,

    %% @doc The id of the crew to which this child has been assigned. This is a
    %%  foreign key linking to a [`vbs_crew.id`](doc://records/vbs_crew/id).
    crew_id,

    %% @doc The child's full name.
    %% @example "Mary Scott", "Gregory Brown"
    name, 

    %% @doc The child's date of birth, stored as {Year, Month, Day}
    %% @example {1998, 12, 22}
    date_of_birth,

    %% @doc The child's gender, either `male` or `female`
    gender, 

    %% @doc The child's grade level in school.
    grade,

    %% @doc A list of ids representing the child's legal guardians. These link
    %%  the child record to adult records by the
    %%  [`vbs_adult.id`](doc://records/vbs_adult/id)
    %% @example [4, 5]
    guardian_ids,

    %% @doc A list of ids, similar to `guardian_ids`, but representing the
    %%  adults that are allowed to pick the children up. These link the child
    %%  record to adult records by
    %%  ['vbs_adult.id`](doc://records/vbs_adult/id).
    pickup_ids,

    %% @doc A list of ids, similar to `guardian_ids` and `pickup_ids`, but
    %%  representing adults that should be contacted if there is an emergency
    %%  involving this child (injury, for example). These link the child record
    %%  to adult records by [`vbs_adult.id`](doc://records/vbs_adult/id).
    emerency_ids,

    %% @doc The child's home church, usually used if they are not a member of
    %%  the hosting church.
    home_church,

    %% @doc If this child is a visitor, this is used to track who invited them,
    %%  or who brought them.
    visitor_of,

    %% @doc Answers the question: Is this child a visitor? Valid values are
    %%  `true` and `false`.
    is_visitor,

    %% @doc The date the child registered, stored as {Year, Month, Day}
    registration_date,

    %% @doc The child's shirt size, stored as a string.
    shirt_size,

    %% @doc Any special needs this child has that should be accomodated.
    special_needs,

    %% @doc Any known allergies this child has.
    allergies,

    %% @doc Additional comments about this child.
    comments}).

%% @doc Information about a crew in the VBS system.
-record(vbs_crew, {

    %% @doc A unique number. This is the record's primary identification and the
    %%  table's primary key.
    id,     % primary key

    %% @doc The crew number.
    number,

    %% @doc The crew type. This is a foreign key on
    %%  [`vbs_crew_type.id`](doc://records/vbs_crew_type/id).
    crew_type_id, % foreign key onto crew_type

    %% @doc The name of the crew, stored as a string.
    name,

    %% @doc Any comments about the crew.
    comments = ""}).

%% @doc Information about a crew type. Crew types are often used when a VBS
%%  program has seperate activities set up for different types of children
%%  (usually based on age). For example, having two type: Elementary and Pre-K
%%  is common when there is a seperate set of activities for smaller children.
-record(vbs_crew_type, {

    %% @doc A unique number. This is the record's primary identification and the
    %%  table's primary key.
    id,

    %% @doc The displayed name of the crew type.
    name}).
   
%% @doc The id counter records are used to keep track of the next valid id for a
%%  specific purpose. This is how the unique id fields in other records is
%%  implmented. 
-record(vbs_id_counter, {

    %% @doc A name for the counter. This is the primary key for the table and
    %%  must be unique.
    %% @example `vbs_adult_id`
    name,              % primary key

    %% @doc The next value for this counter.
    next_value = 0}).

%% @doc Information about workers involved in the VBS program.
-record(vbs_worker, {

    %% @doc A unique number. This is the record's primary identification and the
    %%  table's primary key.
    id,

    %% @doc Links this worker record to a [`vbs_adult`](doc://records/vbs_adult)
    adult_id, % foreign key on adult

    %% @doc The crew this worker is assigned to. This is a link to
    %%  [`vbs_crew.id`](doc://records/vbs_crew/id). The most common way to deal
    %%  with workers who are not assigned to a particular crew is to create a
    %%  special administrative crew and assign all these workers to that crew.
    crew_id = 0,

    %% @doc
    worker_type_id,      % foreign key on worker_type

    %% @doc
    shirt_size,

    %% @doc
    ext_data = []}).

-record(vbs_worker_type, {
    id,     % primary key
    name}).
