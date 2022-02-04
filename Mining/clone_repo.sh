# Clone a given repository, extract any files belonging to the given language, and delete the repository afterwards to save space.
in=$1
language=$2

# Extract the org and name from lines formatted as stars\thttps://github.com/org/name
repo=$(echo $in | cut -d$'\t' -f2);
name_part=$(echo $repo | cut -d"/" -f4-6);
name=$(echo $name_part | cut -d"/" -f2);
org=$(echo $name_part | cut -d"/" -f1);
echo "Cloning $org/$name"
DIR=Repos/$language/$org; \
OUT=Code/$language/$org; \
# Skip repositories for which we already have extracted code files.
if [ -d $OUT/$name ]; then echo "deja vu"; exit; fi;
mkdir -p $DIR; \
mkdir -p $OUT; \

# Clone with depth=1 to only get most recent files, rather than entire history.
if [ ! -d $DIR/$name ]; then
  git clone -q --depth 1 https://github.com/$org/$name $DIR/$name;
fi;

# Extract all language-specific code files from the repository and delete it afterwards.
python3 extract_code.py $language $DIR/$name $OUT/$name;
rm -rf $DIR/$name