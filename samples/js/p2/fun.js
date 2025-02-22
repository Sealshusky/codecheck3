//SOLUTION
//FORBIDDEN sort\s*\(
//Don't sort the array
/**
   Find the second smallest element of a without sorting the array
   You may assume that a has at least two elements, and that
   all elements are integers.
*/
function secondSmallest(a) {
    //HIDE
    let smallest = Math.min(a[0], a[1])
    let nextSmallest = Math.max(a[0], a[1])
    for (let i = 2; i < a.length; i++) {
        if (a[i] < smallest) {
            nextSmallest = smallest;
            smallest = a[i];
        } else if (a[i] < nextSmallest) {
            nextSmallest = a[i];
        }
    }
    return nextSmallest;
    //SHOW ...
}

exports.secondSmallest = secondSmallest
