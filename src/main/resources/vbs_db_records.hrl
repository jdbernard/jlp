% vbs_db_records.erl
%% @author Jonathan Bernard <jdb@jdb-labs.com>
%% @copyright 2010-2011 JDB Labs Inc.

%% #Overview
%%
%%  The VBS database API is centered around the data records:
%% 
%%  * Tables are named after the records. ``vbs_adult`` records are stored in 
%%    a table named ``vbs_adult``.
%%  * The functions that make up the database API are grouped into modules named
%%    after the records on which they operate. The ``vbs_adult`` module contains
%%    the standard VBS database API functions that work with ``vbs_adult``
%%    records.
%%
%% #Record Definitions
%% Here are the record definitions:

%% ## vbs_adult ##
%% @api Information about an adult in the VBS system. 
%% @org records/vbs_adult
-record(vbs_adult, {

    %% @api * A unique number. This is the record's primary identification and
    %%  the table's primary key
    %% @org records/vbs_adult/id
    id,

    %% @api * A unique full name.
    %%
    %%        *Examples:* `"John Smith", "Fae Alice McDonald"`
    %% @org records/vbs_adult/name
    name,

    %% @api * The adult's age (optional).
    %% @org records/vbs_adult/age
    age = 0,

    %% @api * A list of phone numbers (strings). 
    %%
    %%        *Examples:*  `["512-555-1155", "123-456-7890"]`
    %% @org records/vbs_adult/phone_numbers
    phone_numbers,

    %% @api * The adult's address (optional). There is not pre-defined format,
    %%      this is a string that can be formatted s desired (linebreaks are ok,
    %%      for example).
    %%      
    %%      *Example:*
    %%      
    %%          "123 Grant Drive
    %%           Plainsville, TX, 78707"
    %%
    %% @org records/vbs_adult/address 
    address = "",

    %% @api * The adult's email address as a string.
    %% 
    %%        *Example:* `"john_smith@mailco.com"`
    %% @org records/vbs_adult/email 
    email = ""}).

%% ## vbs_attendance ##
%% @api An entry recording a person's attendance.
%% @org records/vbs_attendance
-record(vbs_attendance, {

    %% @api * A unique number. This is the record's primary identification and the
    %%  table's primary key.
    %% @org records/vbs_attendance/id
    id,

    %% @api * The id of person who attended. This is a foreign key onto either the
    %%  [`vbs_worker`](jlp://records/vbs_worker) or
    %%  [`vbs_child`](jlp://records/vbs_child) table, depending on the value of
    %%  the [`person_type`](jlp://records/vbs_attendance/person_type) field.
    %% @org records/vbs_attendance/person_id
    person_id,

    %% @api * The type of person who attended. This determines which table the
    %%  [`person_id`](jlp://records/vbs_attendance/person_id) links on. The
    %%  possible values and the corresponding link tables are:
    %%
    %%    Value   | Link Table
    %%    --------|----------------------------------------
    %%    `child` |[`vbs_child`](jlp://records/vbs_child)
    %%    `worker`|[`vbs_worker`](jlp://records/vbs_worker)
    %%
    %% @org records/vbs_attendance/person_type
    person_type,

    %% @api * The date of attendance, stored as `{Year, Month, Day}.`
    %%
    %%        *Example:* `{2011, 6, 14}`
    %%
    %% @org records/vbs_attendance/date 
    date = {1900, 1, 1},

    %% @api * A timestamp taken when the person was signed in, stored  as
    %%  `{Hour, Minute, Second}`
    %% 
    %%      *Example:* `{5, 22, 13}`
    %% @org records/vbs_attendance/sign_in 
    sign_in = false,        % {hour, minute, second}

    %% @api * A timestamp taken when the person is signed out, stored as
    %% `{Hour, Minute, Second}`
    %% @org records/vbs_attendance/sign_out 
    sign_out = false,       % {hour, minute, second}

    %% @api * A list of {Key, Value} pairs that can be used to store additional
    %%  information. This is intended to allow callers to store optional data,
    %%  or client-specific data, without having to alter the database schema.
    %%  When working with `vbs_attendance` records, a caller should ignore
    %%  `ext_data` values it does not understand
    %% @org records/vbs_attendance/ext_data 
    ext_data = [],

    %% @api * Any comments for the day about this person.
    %% @org records/vbs_attendance/comments 
    comments = ""}).

%% ## vbs_child ##
%% @org records/vbs_child

%% @api Information about a child in the VBS program.
-record(vbs_child, {

    %% @api * A unique number. This is the record's primary identification and the
    %%  table's primary key.
    %% @org records/vbs_child/id
    id,

    %% @api * The id of the crew to which this child has been assigned. This is a
    %%  foreign key linking to a [`vbs_crew.id`](jlp://records/vbs_crew/id).
    %% @org records/vbs_child/crew_id
    crew_id,

    %% @api * The child's full name.
    %% 
    %%      *Example:* `"Mary Scott", "Gregory Brown"`
    %% @org records/vbs_child/name
    name, 

    %% @api * The child's date of birth, stored as `{Year, Month, Day}`
    %% 
    %%      *Example:* `{1998, 12, 22}`
    %% @org records/vbs_child/date_of_birth
    date_of_birth,

    %% @api * The child's gender, either `male` or `female`
    %% @org records/vbs_child/gender
    gender, 

    %% @api * The child's grade level in school.
    %% @org records/vbs_child/grade
    grade,

    %% @api * A list of ids representing the child's legal guardians. These link
    %%  the child record to adult records by the
    %%  [`vbs_adult.id`](jlp://records/vbs_adult/id)
    %% 
    %%      *Example:* `[4, 5]`
    %% @org records/vbs_child/guardian_ids
    guardian_ids,

    %% @api * A list of ids, similar to `guardian_ids`, but representing the
    %%  adults that are allowed to pick the children up. These link the child
    %%  record to adult records by
    %%  ['vbs_adult.id`](jlp://records/vbs_adult/id).
    %% @org records/vbs_child/pickup_ids
    pickup_ids,

    %% @api * A list of ids, similar to `guardian_ids` and `pickup_ids`, but
    %%  representing adults that should be contacted if there is an emergency
    %%  involving this child (injury, for example). These link the child record
    %%  to adult records by [`vbs_adult.id`](jlp://records/vbs_adult/id).
    %% @org records/vbs_child/emerency_ids
    emerency_ids,

    %% @api * The child's home church, usually used if they are not a member of
    %%  the hosting church.
    %% @org records/vbs_child/home_church
    home_church,

    %% @api * If this child is a visitor, this is used to track who invited them,
    %%  or who brought them.
    %% @org records/vbs_child/visitor_of
    visitor_of,

    %% @api * Answers the question: Is this child a visitor? Valid values are
    %%  `true` and `false`.
    %% @org records/vbs_child/is_visitor
    is_visitor,

    %% @api * The date the child registered, stored as `{Year, Month, Day}`
    %% @org records/vbs_child/registration_date
    registration_date,

    %% @api * The child's shirt size, stored as a string.
    %% @org records/vbs_child/shirt_size
    shirt_size,

    %% @api * Any special needs this child has that should be accomodated.
    %% @org records/vbs_child/special_needs
    special_needs,

    %% @api * Any known allergies this child has.
    %% @org records/vbs_child/allergies
    allergies,

    %% @api * Additional comments about this child.
    %% @org records/vbs_child/comments
    comments}).

%% ## vbs_crew ##
%% @api * Information about a crew in the VBS system.
%% @org records/vbs_crew
-record(vbs_crew, {

    %% @api * A unique number. This is the record's primary identification and the
    %%  table's primary key.
    %% @org records/vbs_crew/id
    id,     % primary key

    %% @api * The crew number.
    %% @org records/vbs_crew/number
    number,

    %% @api * The crew type. This is a foreign key on
    %%  [`vbs_crew_type.id`](jlp://records/vbs_crew_type/id).
    %% @org records/vbs_crew/crew_type_id
    crew_type_id, % foreign key onto crew_type

    %% @api * The name of the crew, stored as a string.
    %% @org records/vbs_crew/name
    name,

    %% @api * Any comments about the crew.
    %% @org records/vbs_crew/comments 
    comments = ""}).

%% ## vbs_crew_type ##
%% @api * Information about a crew type. Crew types are often used when a VBS
%%  program has seperate activities set up for different types of children
%%  (usually based on age). For example, having two type: Elementary and Pre-K
%%  is common when there is a seperate set of activities for smaller children.
%% @org records/vbs_crew_type
-record(vbs_crew_type, {

    %% @api * A unique number. This is the record's primary identification and the
    %%  table's primary key.
    %% @org records/vbs_crew_type/id
    id,

    %% @api * The displayed name of the crew type.
    %% @org records/vbs_crew_type/name
    name}).
   
%% ## vbs_id_counter ##
%% @api * The id counter records are used to keep track of the next valid id for a
%%  specific purpose. This is how the unique id fields in other records is
%%  implmented. 
%% @org records/vbs_id_counter
-record(vbs_id_counter, {

    %% @api * A name for the counter. This is the primary key for the table and
    %%  must be unique.
    %% 
    %%      *Example:* `vbs_adult_id`
    %% @org records/vbs_id_counter/name
    name,

    %% @api * The next value for this counter.
    %% @org records/vbs_id_counter/next_value 
    next_value = 0}).

%% ## vbs_worker ##
%% @api * Information about workers involved in the VBS program.
%% @org records/vbs_worker
-record(vbs_worker, {

    %% @api * A unique number. This is the record's primary identification and the
    %%  table's primary key.
    %% @org records/vbs_worker/id
    id,

    %% @api * Links this worker record to a [`vbs_adult`](jlp://records/vbs_adult)
    %% @org records/vbs_worker/adult_id
    adult_id, % foreign key on adult

    %% @api * The crew this worker is assigned to. This is a link to
    %%  [`vbs_crew.id`](jlp://records/vbs_crew/id). The most common way to deal
    %%  with workers who are not assigned to a particular crew is to create a
    %%  special administrative crew and assign all these workers to that crew.
    %% @org records/vbs_worker/crew_id 
    crew_id = 0,

    %% @api *
    %% @org records/vbs_worker/worker_type_id
    worker_type_id,      % foreign key on worker_type

    %% @api *
    %% @org records/vbs_worker/shirt_size
    shirt_size,

    %% @api *
    %% @org records/vbs_worker/ext_data 
    ext_data = []}).

%% ## vbs_worker_type ##
%% @api * Worker types.
%% @org records/vbs_worker_type
-record(vbs_worker_type, {

    %% @api *
    %% @org records/vbs_worker_type/id
    id,

    %% @api *
    %% @org records/vbs_worker_type/name
    name}).
